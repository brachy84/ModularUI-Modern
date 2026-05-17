package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MutableObjectCodec<T> {

    private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields;
    private final InstanceDecoder<T> instanceDecoder;

    private MutableObjectCodec(Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields, InstanceDecoder<T> instanceDecoder) {
        this.fields = fields;
        this.instanceDecoder = instanceDecoder;
    }

    public void forEachField(Consumer<Field<T, ?>> consumer) {
        this.fields.values().forEach(consumer);
    }

    public final MutableCodec<T> codec = new MutableCodec<T>() {

        @Override
        public <J> DataResult<Pair<T, J>> decodeInstance(DynamicOps<J> ops, J input) {
            if (MutableObjectCodec.this.instanceDecoder == null) {
                return DataResult.error(() -> "Instance can not be created since no instance decoder was provided.");
            }
            return MutableObjectCodec.this.instanceDecoder.decodeInstance(ops, input);
        }

        @Override
        public <J> DataResult<Pair<T, J>> decode(DynamicOps<J> ops, J input, T instance) {
            var d = ops.getMap(input);
            var map = d.result();
            if (map.isEmpty()) return DataResult.error(() -> d.error().orElseThrow().message());
            List<String> errors = new ArrayList<>();
            forEachField(f -> {
                var error = decodeField(instance, f, ops, map.get());
                if (error != null) {
                    errors.add(error);
                }
            });
            if (!errors.isEmpty()) {
                return DataResult.error(() ->
                        String.format("Errors while decoding object of type '%s': %s", instance.getClass().getSimpleName(), errors));
            }
            return DataResult.success(new Pair<>(instance, input));
        }

        public DataResult<Pair<T, JsonElement>> decodeJson(JsonObject json) {
            return decode(JsonOps.INSTANCE, json);
        }

        public DataResult<T> parseJson(JsonObject json) {
            return parse(JsonOps.INSTANCE, json);
        }

        public DataResult<Pair<T, JsonElement>> decodeJson(JsonObject json, T instance) {
            return decode(JsonOps.INSTANCE, json, instance);
        }

        public DataResult<T> parseJson(JsonObject json, T instance) {
            return parse(JsonOps.INSTANCE, json, instance);
        }

        @Override
        public <J> DataResult<J> encode(T input, DynamicOps<J> ops, J prefix) {
            var builder = Stream.<Pair<J, J>>builder();
            forEachField(f -> {
                builder.accept(encodeField(input, f, ops));
            });
            return DataResult.success(ops.createMap(builder.build()));
        }
    };

    private <V, J> Pair<J, J> encodeField(T holder, Field<T, V> field, DynamicOps<J> ops) {
        V value = field.fieldDecoder.decodeField(holder);
        DataResult<J> d = field.codec.encodeStart(ops, value);
        return new Pair<>(ops.createString(field.name), d.result().orElseThrow());
    }

    private <V, J> @Nullable String decodeField(T holder, Field<T, V> field, DynamicOps<J> ops, MapLike<J> map) {
        J element = map.get(field.name);
        V value;
        if (element == null) {
            if (field.defaultSupplier == null) {
                return String.format("Field '%s' has no value and is not optional", field.name);
            }
            value = field.defaultSupplier.get();
        } else {
            var d = field.codec.parse(ops, element);
            var res = d.result();
            if (res.isEmpty()) return d.error().orElseThrow().message();
            value = res.get();
        }
        field.fieldEncoder.encodeField(holder, value);
        return null;
    }

    @SuppressWarnings("unchecked")
    private <V> Field<T, V> getField(String name) {
        return (Field<T, V>) this.fields.get(name);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(Class<T> c) {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(InstanceDecoder<T> instanceDecoder) {
        return new Builder<T>().instanceDecoder(instanceDecoder);
    }

    public static <T> Builder<T> builder(Supplier<T> instance) {
        return new Builder<T>().instance(instance);
    }

    public record Field<T, V>(String name, FieldEncoder<T, V> fieldEncoder, FieldDecoder<T, V> fieldDecoder, Codec<V> codec,
                              Supplier<V> defaultSupplier) {

        public boolean hasDefault() {
            return this.defaultSupplier != null;
        }

        public V getDefault() {
            return this.defaultSupplier.get();
        }

        public boolean isUnencodable() {
            return this.codec == null;
        }
    }

    public static class Builder<T> {

        private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields = new Object2ReferenceLinkedOpenHashMap<>();
        private InstanceDecoder<T> instanceDecoder;

        public Builder<T> instanceDecoder(InstanceDecoder<T> instanceDecoder) {
            this.instanceDecoder = instanceDecoder;
            return this;
        }

        public Builder<T> instance(Supplier<T> instance) {
            return instanceDecoder(new InstanceDecoder<>() {
                @Override
                public <J> DataResult<Pair<T, J>> decodeInstance(DynamicOps<J> ops, J input) {
                    return DataResult.success(new Pair<>(instance.get(), input));
                }
            });
        }

        public <V> Builder<T> add(String name, FieldEncoder<T, V> fieldEncoder, FieldDecoder<T, V> fieldDecoder, Codec<V> codec) {
            return addDynOpt(name, fieldEncoder, fieldDecoder, codec, null);
        }

        public <V> Builder<T> addOpt(String name, FieldEncoder<T, V> fieldEncoder, FieldDecoder<T, V> fieldDecoder, Codec<V> codec, V defValue) {
            return addDynOpt(name, fieldEncoder, fieldDecoder, codec, () -> defValue);
        }

        public <V> Builder<T> addDynOpt(String name, FieldEncoder<T, V> fieldEncoder, FieldDecoder<T, V> fieldDecoder, Codec<V> codec, Supplier<V> defaultSupplier) {
            this.fields.put(name, new Field<>(name, fieldEncoder, fieldDecoder, codec, defaultSupplier));
            return this;
        }

        public <V> Builder<T> addUncodable(String name, FieldDecoder<T, V> fieldDecoder) {
            return addUncodable(name, fieldDecoder, Objects::isNull);
        }

        public <V> Builder<T> addUncodable(String name, FieldDecoder<T, V> fieldDecoder, Predicate<V> emptyTest) {
            return add(name, (holder, value) -> {
                throw new IllegalArgumentException("Unable to write field");
            }, holder -> {
                if (!emptyTest.test(fieldDecoder.decodeField(holder))) {
                    throw new IllegalArgumentException("Unable to read field");
                }
                return null;
            }, null);
        }

        public MutableObjectCodec<T> build() {
            return new MutableObjectCodec<>(this.fields, this.instanceDecoder);
        }
    }
}
