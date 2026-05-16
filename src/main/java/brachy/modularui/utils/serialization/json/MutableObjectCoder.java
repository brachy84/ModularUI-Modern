package brachy.modularui.utils.serialization.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MutableObjectCoder<T> {

    private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields;

    private MutableObjectCoder(Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields) {
        this.fields = fields;
    }

    public void forEachField(Consumer<Field<T, ?>> consumer) {
        this.fields.values().forEach(consumer);
    }

    public JsonElement encodeJson(T holder) {
        JsonObject json = new JsonObject();
        forEachField(f -> encodeField(holder, f, json));
        return json;
    }

    private <V> void encodeField(T holder, Field<T, V> field, JsonObject json) {
        json.add(field.name, field.jsonCoder.encodeJson(field.fieldDecoder.decodeField(holder)));
    }

    public void decodeJson(T holder, JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) throw new IllegalArgumentException();
        JsonObject json = jsonElement.getAsJsonObject();
        forEachField(f -> decodeField(holder, f, json));
    }

    private <V> void decodeField(T holder, Field<T, V> field, JsonElement json) {
        field.fieldEncoder.encodeField(holder, field.jsonCoder.decodeJson(json).orElseThrow());
    }

    @SuppressWarnings("unchecked")
    private <V> Field<T, V> getField(String name) {
        return (Field<T, V>) this.fields.get(name);
    }

    public record Field<T, V>(String name, JsonCoder<V> jsonCoder, FieldEncoder<T, V> fieldEncoder, FieldDecoder<T, V> fieldDecoder) {}

    public static class Builder<T> {

        private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields = new Object2ReferenceLinkedOpenHashMap<>();

        public <V> Builder<T> add(String name, JsonCoder<V> jsonCoder, FieldEncoder<T, V> fieldEncoder, FieldDecoder<T, V> fieldDecoder) {
            this.fields.put(name, new Field<>(name, jsonCoder, fieldEncoder, fieldDecoder));
            return this;
        }

        public <V> Builder<T> addUncodable(String name, FieldDecoder<T, V> fieldDecoder) {
            return addUncodable(name, fieldDecoder, Objects::isNull);
        }

        public <V> Builder<T> addUncodable(String name, FieldDecoder<T, V> fieldDecoder, Predicate<V> emptyTest) {
            return add(name, JsonCoder.EMPTY, (holder, value) -> {
                throw new IllegalArgumentException("Unable to write field");
            }, holder -> {
                if (!emptyTest.test(fieldDecoder.decodeField(holder))) {
                    throw new IllegalArgumentException("Unable to read field");
                }
                return null;
            });
        }

        public MutableObjectCoder<T> build() {
            return new MutableObjectCoder<>(this.fields);
        }
    }
}
