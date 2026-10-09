package brachy.modularui.utils.serialization.codec;

import brachy.modularui.api.codec.InstanceMapDecoder;
import brachy.modularui.editor.Option;
import brachy.modularui.editor.Options;
import brachy.modularui.utils.EqualityTest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import com.google.common.collect.AbstractIterator;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * This can handle (de)serialization and conversion to various data types, editing properties, copying properties and applying default
 * properties to (partially) mutable objects.
 *
 * @param <T> type of property
 */
@Accessors(fluent = true)
public class MutableObjectCodec<T> extends ExtendedMutableMapCodec<T> implements Options<T> {

    private final List<Field<T, ?>> fields;
    private final InstanceMapDecoder<T> instanceDecoder;
    private final UnaryOperator<T> baseCopy;
    private final EqualityTest<T> equals;
    private final Codec<T> wrapped;

    private MutableObjectCodec(List<Field<T, ?>> fields, InstanceMapDecoder<T> instanceDecoder, UnaryOperator<T> baseCopy,
                               EqualityTest<T> equals, Codec<T> wrapped) {
        this.fields = Collections.unmodifiableList(fields);
        this.instanceDecoder = instanceDecoder;
        this.baseCopy = baseCopy;
        this.equals = equals != null ? equals : EqualityTest.defaultTester();
        this.wrapped = wrapped;
    }

    public Optional<Field<T, ?>> findField(String name) {
        return this.fields.stream().filter(f -> f.name().equals(name)).findFirst();
    }

    public void forEachField(Consumer<Field<T, ?>> consumer) {
        this.fields.forEach(consumer);
    }

    public boolean testEachField(Predicate<Field<T, ?>> test) {
        for (Field<T, ?> f : this.fields) {
            if (!test.test(f)) return false;
        }
        return true;
    }

    public boolean hasAnyField(JsonObject json) {
        return !testEachField(f -> {
            if (json.has(f.name())) return false;
            if (f.altNames() != null) {
                for (String alt : f.altNames()) {
                    if (json.has(alt)) return false;
                }
            }
            return true;
        });
    }

    public boolean canCopy() {
        return this.baseCopy != null;
    }

    @Override
    public T copy(T from) {
        if (from == null) return null;
        if (this.baseCopy == null) {
            throw new IllegalStateException("Can't copy instance since no base copy function is supplied.");
        }
        T copy = this.baseCopy.apply(from);
        if (copy == null) return null;
        return copyFields(from, copy);
    }

    @Override
    public <B extends T> B copyFields(T from, B to) {
        if (from == null || to == null) throw new NullPointerException();
        forEachField(f -> f.copyValue(from, to));
        return to;
    }

    public void applyDefaults(T instance) {
        forEachField(f -> f.applyDefault(instance));
    }

    @Override
    public boolean canDecodeInstance() {
        return this.instanceDecoder != null || this.wrapped != null;
    }

    public boolean areFieldsDefaultExcept(T instance, String exception) {
        return areFieldsDefaultExcept(instance, f -> f.name().equals(exception));
    }

    public boolean areFieldsDefaultExcept(T instance, String... exceptions) {
        return areFieldsDefaultExcept(instance, f -> ArrayUtils.contains(exceptions, f.name()));
    }

    public boolean areFieldsDefaultExcept(T instance, Predicate<Field<T, ?>> test) {
        for (Field<T, ?> f : fields) {
            if (!test.test(f) && !f.isValueDefault(instance)) {
                return false;
            }
        }
        return true;
        //return testEachField(f -> !test.test(f) && !f.isValueDefault(instance));
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(Class<T> c) {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(InstanceMapDecoder<T> instanceDecoder) {
        return new Builder<T>().instanceDecoder(instanceDecoder);
    }

    public static <T> Builder<T> builder(Supplier<T> instance) {
        return new Builder<T>().instance(instance);
    }

    @Override
    public <J> Stream<J> keys(DynamicOps<J> ops) {
        return this.fields.stream().map(Field::name).map(ops::createString);
    }

    @Override
    public <J> DataResult<T> decode(DynamicOps<J> ops, MapLike<J> input, T instance) {
        if (Objects.equals(input, ops.empty())) {
            return DataResult.success(instance);
        }
        List<String> errors = new ArrayList<>();
        forEachField(f -> {
            var error = f.decode(instance, ops, input);
            if (error != null) errors.add(error);
        });
        if (!errors.isEmpty()) {
            return DataResult.error(() -> String.format("Errors while decoding object of type '%s': %s", instance.getClass().getSimpleName(), errors));
        }
        return DataResult.success(instance);
    }

    @Override
    public <J> RecordBuilder<J> encode(T input, DynamicOps<J> ops, RecordBuilder<J> builder) {
        if (input == null) {
            return builder;
        }
        if (this.wrapped != null) {
            if (this.wrapped instanceof MapCodec.MapCodecCodec<T> mcc) {
                var codec = mcc.codec();
                builder = codec.encode(input, ops, builder);
            } else {
                var prefix = builder.build(this.wrapped.encode(input, ops, ops.empty()));
                var res = prefix.result();
                if (res.isEmpty()) {
                    builder.withErrorsFrom(prefix);
                } else {
                    var d = ops.getMapValues(res.get());
                    var res2 = d.result();
                    if (res2.isEmpty()) {
                        builder.withErrorsFrom(d);
                    } else {
                        RecordBuilder<J> finalBuilder = builder;
                        res2.get().forEach(p -> finalBuilder.add(p.getFirst(), p.getSecond()));
                    }
                }
            }
        }
        RecordBuilder<J> finalBuilder = builder;
        forEachField(f -> f.encode(input, ops, finalBuilder));
        return builder;
    }

    @Override
    public <J> DataResult<T> decodeInstance(DynamicOps<J> ops, MapLike<J> input) {
        if (this.wrapped != null) {
            if (this.wrapped instanceof MapCodec.MapCodecCodec<T> mcc) {
                return mcc.codec().decode(ops, input);
            }
            return this.wrapped.parse(ops, ops.createMap(input.entries()));
        }
        if (this.instanceDecoder != null) return this.instanceDecoder.decodeInstance(ops, input);
        return DataResult.error(() -> "Instance can not be created since no instance decoder or wrapped codec was provided.");
    }

    @Override
    public String convertToString(T instance, int indent) {
        if (instance == null) return "null";
        StringBuilder b = new StringBuilder();
        b.append(instance.getClass().getSimpleName())
                .append(" {");
        if (indent >= 0) b.append("\n");
        forEachField(f -> {
            if (indent >= 0) b.append("  ".repeat(indent + 1));
            f.convertToString(instance, b, indent + 1);
            b.append(",");
            if (indent >= 0) b.append('\n');
            else b.append(" ");
        });
        b.deleteCharAt(b.length() - 2);
        if (indent < 0) {
            b.deleteCharAt(b.length() - 1);
        } else {
            b.append("  ".repeat(indent));
        }
        return b.append("}").toString();
    }

    @Override
    public boolean areEqual(@NotNull T t1, @NotNull T t2) {
        return this.equals.areEqual(t1, t2);
    }

    @Override
    public @NotNull Iterator<Option<T, ?>> iterator() {
        return new AbstractIterator<>() {

            private final Iterator<Field<T, ?>> it = MutableObjectCodec.this.fields.iterator();

            @Override
            protected Option<T, ?> computeNext() {
                return this.it.hasNext() ? this.it.next() : endOfData();
            }
        };
    }

    @Override
    public Option<T, ?> getOption(String name) {
        return findField(name).orElse(null);
    }

    public static class Builder<T> {

        private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields = new Object2ReferenceLinkedOpenHashMap<>();
        private InstanceMapDecoder<T> instanceDecoder;
        private UnaryOperator<T> baseCopy;
        private EqualityTest<T> equals;
        private Codec<T> wrapped;

        private Field<T, ?> lastField;

        /**
         * Sets the instance decoder. This is needed when parsing from JSON. The decoder should create a new instance
         * and decode any necessary data for that. If the object has a no-arg constructor, then {@link #instance(Supplier)} should be used.
         * If this setter is used, then {@link #baseCopy(UnaryOperator)} must also be used.
         */
        public Builder<T> instanceDecoder(InstanceMapDecoder<T> instanceDecoder) {
            this.instanceDecoder = instanceDecoder;
            return this;
        }

        public Builder<T> instanceDecoder(MapDecoder<T> instanceDecoder) {
            return instanceDecoder(instanceDecoder::decode);
        }

        /**
         * Sets the instance supplier. This is needed when parsing from JSON and for copying. This MUST always return a new instance.
         */
        public Builder<T> instance(Supplier<T> instance) {
            return instanceDecoder(new InstanceMapDecoder<>() {
                @Override
                public <J> DataResult<T> decodeInstance(DynamicOps<J> ops, MapLike<J> input) {
                    return DataResult.success(instance.get());
                }
            }).baseCopy(t -> instance.get());
        }

        /**
         * Sets the base copy function. This creates a new instance, but without setting any mutable properties. This MUST always return
         * a new instance. If the object has a no-arg constructor, then {@link #instance(Supplier)} should be used.
         */
        public Builder<T> baseCopy(UnaryOperator<T> baseCopy) {
            this.baseCopy = t -> {
                T copy = baseCopy.apply(t);
                if (copy == t) throw new IllegalArgumentException("The copy function must return a new object!");
                return copy;
            };
            return this;
        }

        public Builder<T> equalityTest(EqualityTest<T> equals) {
            this.equals = equals;
            return this;
        }

        /**
         * When this object is encoded and decoded this codec will be called first. This is useful when this object is based on another
         * object which already has a codec. Example: {@link brachy.modularui.drawable.text.ModularComponent#CODEC ModularComponent.CODEC}
         */
        public Builder<T> wrapped(Codec<T> codec) {
            this.wrapped = codec;
            return this;
        }

        public Builder<T> addField(Field<T, ?> field) {
            this.lastField = field;
            this.fields.put(field.name(), field);
            return this;
        }

        public Builder<T> addFieldsOfSameType(MutableObjectCodec<T> parent) {
            parent.fields.forEach(f -> this.fields.put(f.name(), f));
            return this;
        }

        public <P> Builder<T> addFieldsOf(MutableObjectCodec<P> parent, Function<T, P> converter) {
            parent.fields.forEach(f -> this.fields.put(f.name(), f.copyToType(converter)));
            return this;
        }

        public Builder<T> addFieldOfSameType(MutableObjectCodec<T> parent, String fieldName) {
            return addFieldOfSameType(parent, fieldName, fieldName);
        }

        public Builder<T> addFieldOfSameType(MutableObjectCodec<T> parent, String fieldName, String newName) {
            parent.findField(fieldName).ifPresentOrElse(f -> addField(f.copy(newName)), () -> {
                throw new IllegalArgumentException("MutableObjectCodec does not have field '" + fieldName + "'.");
            });
            return this;
        }

        public <P> Builder<T> addFieldOf(MutableObjectCodec<P> parent, Function<T, P> converter, String fieldName) {
            return addFieldOf(parent, converter, fieldName, fieldName);
        }

        public <P> Builder<T> addFieldOf(MutableObjectCodec<P> parent, Function<T, P> converter, String fieldName, String newName) {
            parent.findField(fieldName).ifPresentOrElse(f -> addField(f.copyToType(newName, converter)), () -> {
                throw new IllegalArgumentException("MutableObjectCodec does not have field '" + fieldName + "'.");
            });
            return this;
        }

        public <V> Builder<T> add(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, MapCodec<V> codec) {
            return addDynOpt(name, fieldWriter, fieldReader, codec.codec(), null);
        }

        /**
         * Adds a new non-optional, mutable property.
         *
         * @see #addDynOpt(String, FieldWriter, FieldReader, Codec, Supplier)
         */
        public <V> Builder<T> add(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, null);
        }

        public <V> Builder<T> addOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                     MapCodec<V> codec, @Nullable V defValue) {
            return addOpt(name, fieldWriter, fieldReader, codec.codec(), defValue);
        }

        /**
         * Adds a new optional, mutable property with a const default value.
         *
         * @param defValue default value, if this is null, the default value is null and this property is still considered optional
         * @see #addDynOpt(String, FieldWriter, FieldReader, Codec, Supplier)
         */
        public <V> Builder<T> addOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                     Codec<V> codec, @Nullable V defValue) {
            Objects.requireNonNull(name, "Name of field must not be null!");
            Objects.requireNonNull(fieldWriter, "Field encoder must not be null!");
            Objects.requireNonNull(fieldReader, "Field decoder must not be null!");
            return addField(new Field<>(name, fieldWriter, fieldReader, codec, () -> defValue, false));
        }

        public <V> Builder<T> addDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, MapCodec<V> codec,
                                        @Nullable Supplier<V> defaultSupplier) {
            return addDynOpt(name, fieldWriter, fieldReader, codec.codec(), defaultSupplier);
        }

        /**
         * Adds a new optional, mutable property with a dynamic default value.
         *
         * @param name            name of the property, mostly used for en-/decoding
         * @param fieldWriter     writes a value to the field
         * @param fieldReader     reads a value from the field
         * @param codec           handles en-/decoding of a value
         * @param defaultSupplier supplier for a default value, if this is non-null, this property is marked as optional
         * @param <V>             type of value
         * @return this
         * @throws NullPointerException if name, fieldEncoder or fieldDecoder is null
         */
        public <V> Builder<T> addDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec,
                                        @Nullable Supplier<V> defaultSupplier) {
            Objects.requireNonNull(name, "Name of field must not be null!");
            Objects.requireNonNull(fieldWriter, "Field writer must not be null!");
            Objects.requireNonNull(fieldReader, "Field reader must not be null!");
            return addField(new Field<>(name, fieldWriter, fieldReader, codec, defaultSupplier, defaultSupplier != null)
                    .encodeWhen(Field.EncodeWhen.ALWAYS));
        }

        public <V> Builder<T> addUnencodable(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader) {
            return add(name, fieldWriter, fieldReader, (Codec<V>) null);
        }

        public <V> Builder<T> addUnencodableOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                @Nullable V defaultValue) {
            return addOpt(name, fieldWriter, fieldReader, (Codec<V>) null, defaultValue);
        }

        /**
         * Adds a new unencodable, optional, mutable property with a dynamic default value.
         * Unencodable means it cannot be converted to a data format like JSON. This is the case for functions.
         * These unencodable values are still important for copying and applying default values.
         *
         * @see #addDynOpt(String, FieldWriter, FieldReader, Codec, Supplier)
         */
        public <V> Builder<T> addUnencodableDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                   @Nullable Supplier<V> defaultSupplier) {
            return addDynOpt(name, fieldWriter, fieldReader, (Codec<V>) null, defaultSupplier);
        }

        /**
         * Adds a decoder field. These are additional fields which can only decode, but not encode, copy or convert to string.
         * This is useful when to give JSON additional flexibility.
         */
        public <V> Builder<T> addDecoder(String name, FieldWriter<T, V> fieldWriter, Decoder<V> decoder) {
            Objects.requireNonNull(name, "Name of field must not be null!");
            Objects.requireNonNull(fieldWriter, "Field writer must not be null!");
            Objects.requireNonNull(decoder, "Field decoder must not be null!");
            return addField(new Field<>(name, fieldWriter, decoder));
        }

        public Builder<T> removeField(String name) {
            this.fields.remove(name);
            return this;
        }

        public Builder<T> alwaysEncode() {
            return encodeWhen(Field.EncodeWhen.ALWAYS);
        }

        public Builder<T> neverEncode() {
            return encodeWhen(Field.EncodeWhen.NEVER);
        }

        public Builder<T> encodeWhenChanged() {
            return encodeWhen(Field.EncodeWhen.CHANGED);
        }

        public Builder<T> encodeWhen(Field.EncodeWhen encodeWhen) {
            return doOnField(f -> f.encodeWhen(encodeWhen));
        }

        public Builder<T> neverWriteDefault() {
            return writeDefault(false);
        }

        public Builder<T> writeDefault(boolean writeDefault) {
            return doOnField(f -> f.writeDefault(writeDefault));
        }

        public Builder<T> alias(String... alias) {
            return doOnField(f -> f.altNames(alias));
        }

        private Builder<T> doOnField(Consumer<Field<T, ?>> consumer) {
            if (this.lastField != null) {
                consumer.accept(this.lastField);
            }
            return this;
        }

        public MutableObjectCodec<T> build() {
            return new MutableObjectCodec<>(new ArrayList<>(this.fields.values()), this.instanceDecoder, this.baseCopy, this.equals, this.wrapped);
        }
    }
}
