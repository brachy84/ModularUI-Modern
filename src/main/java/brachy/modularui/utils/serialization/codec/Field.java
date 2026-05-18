package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Accessors(fluent = true)
public final class Field<T, V> {

    @Getter private final String name;
    @Getter private final FieldWriter<T, V> fieldWriter;
    @Getter private final FieldReader<T, V> fieldReader;
    @Getter private final Codec<V> codec;
    @Getter private final Supplier<V> defaultSupplier;
    @Getter
    @Setter
    private String[] altNames;
    @Getter
    @Setter
    private boolean alwaysEncode;

    public Field(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec, Supplier<V> defaultSupplier) {
        this.name = name;
        this.fieldWriter = fieldWriter;
        this.fieldReader = fieldReader;
        this.codec = codec;
        this.defaultSupplier = defaultSupplier;
    }

    public boolean hasDefault() {
        return this.defaultSupplier != null;
    }

    public V getDefault() {
        return this.defaultSupplier.get();
    }

    public boolean isUnencodable() {
        return this.codec == null;
    }

    public <J> @Nullable String encode(T holder, DynamicOps<J> ops, Stream.Builder<Pair<J, J>> map) {
        V value = this.fieldReader.readField(holder);
        if (isUnencodable()) {
            if (!isEmpty(value)) {
                return String.format("Field '%s' is unencodeable, but the value is not empty", this.name);
            }
            return null;
        }
        if (value == null) {
            if (hasDefault()) return null;
            return String.format("Field '%s' is not optional, but is trying to encode a null value", this.name);
        }
        if (!alwaysEncode() && hasDefault() && Objects.equals(value, getDefault())) {
            return null;
        }
        var d = this.codec.encodeStart(ops, value);
        var res = d.result();
        if (res.isEmpty()) return d.error().orElseThrow().message();
        map.accept(new Pair<>(ops.createString(this.name), res.get()));
        return null;
    }

    public <J> @Nullable String decode(T holder, DynamicOps<J> ops, MapLike<J> map) {
        J element = map.get(this.name);
        if (element == null && this.altNames != null) {
            for (String alt : this.altNames) {
                element = map.get(alt);
                if (element != null) break;
            }
        }
        V value;
        if (element == null) {
            if (!hasDefault()) {
                return isUnencodable() ? null : String.format("Field '%s' has no value and is not optional", this.name);
            }
            value = getDefault();
        } else if (isUnencodable()) {
            return String.format("Field '%s' is unencodable, but data still contains value", this.name);
        } else if (this.codec instanceof MutableCodec<V> mutableCodec) {
            value = this.fieldReader.readField(holder);
            if (value == null) {
                if (mutableCodec.canDecodeInstance()) {
                    var d = mutableCodec.parseInstance(ops, element);
                    var res = d.result();
                    if (res.isEmpty()) return d.error().orElseThrow().message();
                    value = res.get();
                }
                if (value == null && hasDefault()) value = getDefault();
                if (value == null) {
                    return String.format("Field '%s' is unable to decode instance and the holder has no default value and this property has no default value", this.name);
                }
            }
            var d = mutableCodec.parse(ops, element, value);
            var res = d.result();
            if (res.isEmpty()) return d.error().orElseThrow().message();
            value = res.get();
        } else {
            var d = this.codec.parse(ops, element);
            var res = d.result();
            if (res.isEmpty()) return d.error().orElseThrow().message();
            value = res.get();
        }
        this.fieldWriter.writeField(holder, value);
        return null;
    }

    public void copy(T from, T to) {
        V value = this.fieldReader.readField(from);
        if (this.codec instanceof MutableObjectCodec<V> mutableObjectCodec) {
            value = mutableObjectCodec.copy(value);
        }
        this.fieldWriter.writeField(to, value);
    }

    public void applyDefault(T instance) {
        if (hasDefault()) {
            this.fieldWriter.writeField(instance, getDefault());
        }
    }

    public boolean isEmpty(V value) {
        return value == null;
    }
}
