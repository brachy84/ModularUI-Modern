package brachy.modularui.utils.serialization.codec;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import com.google.common.base.CaseFormat;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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
public class MutableObjectCodec<T> implements MutableCodec<T> {

    private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields;
    private final InstanceDecoder<T> instanceDecoder;
    private final UnaryOperator<T> baseCopy;

    private MutableObjectCodec(Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields,
                               InstanceDecoder<T> instanceDecoder, UnaryOperator<T> baseCopy) {
        this.fields = fields;
        this.instanceDecoder = instanceDecoder;
        this.baseCopy = baseCopy;
    }

    public void forEachField(Consumer<Field<T, ?>> consumer) {
        this.fields.values().forEach(consumer);
    }

    @SuppressWarnings("unchecked")
    private <V> Field<T, V> getField(String name) {
        return (Field<T, V>) this.fields.get(name);
    }

    public T copy(T from) {
        if (from == null) return null;
        if (this.baseCopy == null) {
            throw new IllegalStateException("Can't copy instance since no base copy function is supplied.");
        }
        T copy = this.baseCopy.apply(from);
        copyFields(from, copy);
        return copy;
    }

    public void copyFields(T from, T to) {
        forEachField(f -> f.copy(from, to));
    }

    public void applyDefaults(T instance) {
        forEachField(f -> f.applyDefault(instance));
    }

    @Override
    public <J> DataResult<Pair<T, J>> decodeInstance(DynamicOps<J> ops, J input) {
        if (MutableObjectCodec.this.instanceDecoder == null) {
            return DataResult.error(() -> "Instance can not be created since no instance decoder was provided.");
        }
        return MutableObjectCodec.this.instanceDecoder.decodeInstance(ops, input);
    }

    @Override
    public boolean canDecodeInstance() {
        return this.instanceDecoder != null;
    }

    @Override
    public <J> DataResult<Pair<T, J>> decode(DynamicOps<J> ops, J input, T instance) {
        if (Objects.equals(input, ops.empty())) {
            return DataResult.success(new Pair<>(null, ops.empty()));
        }
        var d = ops.getMap(input);
        var map = d.result();
        if (map.isEmpty()) return DataResult.error(() -> d.error().orElseThrow().message());
        List<String> errors = new ArrayList<>();
        forEachField(f -> {
            var error = f.decode(instance, ops, map.get());
            if (error != null) errors.add(error);
        });
        if (!errors.isEmpty()) {
            return DataResult.error(() ->
                    String.format("Errors while decoding object of type '%s': %s", instance.getClass().getSimpleName(), errors));
        }
        return DataResult.success(new Pair<>(instance, input));
    }

    @Override
    public <J> DataResult<J> encode(T input, DynamicOps<J> ops, J prefix) {
        if (input == null) {
            return DataResult.success(ops.empty());
        }
        var builder = Stream.<Pair<J, J>>builder();
        List<String> errors = new ArrayList<>();
        forEachField(f -> {
            var error = f.encode(input, ops, builder);
            if (error != null) errors.add(error);
        });
        if (!errors.isEmpty()) {
            return DataResult.error(() ->
                    String.format("Errors while encoding object of type '%s': %s", input.getClass().getSimpleName(), errors));
        }
        return DataResult.success(ops.createMap(builder.build()));
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

    public static <T extends IDrawable> Builder<T> drawableBuilder(Class<T> type) {
        return new DrawableBuilder<>(type.getTypeName());
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(String typeName) {
        return new DrawableBuilder<>(typeName);
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(Class<T> type, String typeName) {
        return new DrawableBuilder<>(typeName);
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(String typeName, InstanceDecoder<T> instanceDecoder) {
        return new DrawableBuilder<T>(typeName).instanceDecoder(instanceDecoder);
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(String typeName, Supplier<T> instance) {
        return new DrawableBuilder<T>(typeName).instance(instance);
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(Class<T> type, InstanceDecoder<T> instanceDecoder) {
        return new DrawableBuilder<T>(type.getSimpleName()).instanceDecoder(instanceDecoder);
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(Supplier<T> instance) {
        return drawableBuilder(instance.get().getTypeName(), instance);
    }

    public static <T extends IDrawable> Builder<T> drawableBuilder(Class<T> type, Supplier<T> instance) {
        return new DrawableBuilder<T>(type.getSimpleName()).instance(instance);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(Class<T> type) {
        return new WidgetBuilder<>(type.getTypeName());
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(String typeName) {
        return new WidgetBuilder<>(typeName);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(Class<T> type, String typeName) {
        return new WidgetBuilder<>(typeName);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(String typeName, InstanceDecoder<T> instanceDecoder) {
        return new WidgetBuilder<T>(typeName).instanceDecoder(instanceDecoder);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(String typeName, Supplier<T> instance) {
        return new WidgetBuilder<T>(typeName).instance(instance);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(Class<T> type, InstanceDecoder<T> instanceDecoder) {
        return new WidgetBuilder<T>(type.getSimpleName()).instanceDecoder(instanceDecoder);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(Class<T> type, Supplier<T> instance) {
        return new WidgetBuilder<T>(type.getSimpleName()).instance(instance);
    }

    public static <T extends IWidget> Builder<T> widgetBuilder(Supplier<T> instance) {
        return widgetBuilder(instance.get().getTypeName(), instance);
    }

    public record Field<T, V>(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec,
                              Supplier<V> defaultSupplier, Predicate<V> emptyTester, String[] altNames) {

        public boolean hasDefault() {
            return this.defaultSupplier != null;
        }

        public V getDefault() {
            return this.defaultSupplier.get();
        }

        public boolean isUnencodable() {
            return this.codec == null;
        }

        private <J> @Nullable String encode(T holder, DynamicOps<J> ops, Stream.Builder<Pair<J, J>> map) {
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
            var d = this.codec.encodeStart(ops, value);
            var res = d.result();
            if (res.isEmpty()) return d.error().orElseThrow().message();
            map.accept(new Pair<>(ops.createString(this.name), res.get()));
            return null;
        }

        private <J> @Nullable String decode(T holder, DynamicOps<J> ops, MapLike<J> map) {
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
                    return String.format("Field '%s' has no value and is not optional", this.name);
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
            return this.emptyTester != null ? this.emptyTester.test(value) : value == null;
        }
    }

    public static class Builder<T> {

        private final Object2ReferenceLinkedOpenHashMap<String, Field<T, ?>> fields = new Object2ReferenceLinkedOpenHashMap<>();
        private InstanceDecoder<T> instanceDecoder;
        private UnaryOperator<T> baseCopy;
        private CodecRegistry<T> registry;
        private String[] names;

        /**
         * Sets the instance decoder. This is needed when parsing from JSON. The decoder should create a new instance
         * and decode any necessary data for that. If the object has a no-arg constructor, then {@link #instance(Supplier)} should be used.
         * If this setter is used, then {@link #baseCopy(UnaryOperator)} must also be used.
         *
         * @param instanceDecoder instance decoder
         * @return this
         */
        public Builder<T> instanceDecoder(InstanceDecoder<T> instanceDecoder) {
            this.instanceDecoder = instanceDecoder;
            return this;
        }

        /**
         * Sets the instance supplier. This is needed when parsing from JSON and for copying. This MUST always return a new instance.
         *
         * @param instance instance supplier
         * @return this
         */
        public Builder<T> instance(Supplier<T> instance) {
            return instanceDecoder(new InstanceDecoder<>() {
                @Override
                public <J> DataResult<Pair<T, J>> decodeInstance(DynamicOps<J> ops, J input) {
                    return DataResult.success(new Pair<>(instance.get(), input));
                }
            }).baseCopy(t -> instance.get());
        }

        /**
         * Sets the base copy function. This creates a new instance, but without setting any mutable properties. This MUST always return
         * a new instance. If the object has a no-arg constructor, then {@link #instance(Supplier)} should be used.
         *
         * @param baseCopy base copy function
         * @return this
         */
        public Builder<T> baseCopy(UnaryOperator<T> baseCopy) {
            this.baseCopy = t -> {
                T copy = baseCopy.apply(t);
                if (copy == t) throw new IllegalArgumentException("The copy function must return a new object!");
                return copy;
            };
            return this;
        }

        public <V> Builder<T> add(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, null);
        }

        public <V> Builder<T> add(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec, String @Nullable ... altNames) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, null, altNames);
        }

        public <V> Builder<T> add(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                  Codec<V> codec, @Nullable Predicate<V> emptyTester) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, null, emptyTester);
        }

        /**
         * Adds a new non-optional, mutable property.
         *
         * @see #addDynOpt(String, FieldWriter, FieldReader, Codec, Supplier, Predicate)
         */
        public <V> Builder<T> add(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                  Codec<V> codec, @Nullable Predicate<V> emptyTester, String @Nullable ... altNames) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, null, emptyTester, altNames);
        }

        public <V> Builder<T> addOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                     Codec<V> codec, @Nullable V defValue) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, () -> defValue);
        }

        public <V> Builder<T> addOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                     Codec<V> codec, @Nullable V defValue, String @Nullable ... altNames) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, () -> defValue, altNames);
        }

        public <V> Builder<T> addOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                     Codec<V> codec, @Nullable V defValue, @Nullable Predicate<V> emptyTester) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, () -> defValue, emptyTester, (String[]) null);
        }

        /**
         * Adds a new optional, mutable property with a const default value.
         *
         * @param defValue default value, if this is null, the default value is null and this property is still considered optional
         * @see #addDynOpt(String, FieldWriter, FieldReader, Codec, Supplier, Predicate)
         */
        public <V> Builder<T> addOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                     Codec<V> codec, @Nullable V defValue, @Nullable Predicate<V> emptyTester, String @Nullable ... altNames) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, () -> defValue, emptyTester, altNames);
        }

        public <V> Builder<T> addDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                        Codec<V> codec, @Nullable Supplier<V> defaultSupplier) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, defaultSupplier, null, (String[]) null);
        }

        public <V> Builder<T> addDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                        Codec<V> codec, @Nullable Supplier<V> defaultSupplier, String @Nullable ... altNames) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, defaultSupplier, null, altNames);
        }

        public <V> Builder<T> addDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec,
                                        @Nullable Supplier<V> defaultSupplier, @Nullable Predicate<V> emptyTester) {
            return addDynOpt(name, fieldWriter, fieldReader, codec, defaultSupplier, emptyTester, (String[]) null);
        }

        /**
         * Adds a new optional, mutable property with a dynamic default value.
         *
         * @param name            name of the property, mostly used for en-/decoding
         * @param fieldWriter     writes a value to the field
         * @param fieldReader     reads a value from the field
         * @param codec           handles en-/decoding of a value
         * @param defaultSupplier supplier for a default value, if this is non-null, this property is marked as optional
         * @param emptyTester     a test function to test whether a value is empty, this is currently only used for unencodable values
         * @param <V>             type of value
         * @return this
         * @throws NullPointerException if name, fieldEncoder or fieldDecoder is null
         */
        public <V> Builder<T> addDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, Codec<V> codec,
                                        @Nullable Supplier<V> defaultSupplier, @Nullable Predicate<V> emptyTester, String @Nullable ... altNames) {
            Objects.requireNonNull(name, "Name of field must not be null!");
            Objects.requireNonNull(fieldWriter, "Field encoder must not be null!");
            Objects.requireNonNull(fieldReader, "Field decoder must not be null!");
            this.fields.put(name, new Field<>(name, fieldWriter, fieldReader, codec, defaultSupplier, emptyTester, altNames));
            return this;
        }

        public <V> Builder<T> addUnencodable(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, null, null, (String[]) null);
        }

        public <V> Builder<T> addUnencodable(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader, String @Nullable ... altNames) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, null, null, altNames);
        }

        public <V> Builder<T> addUnencodable(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                             @Nullable Predicate<V> emptyTest) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, null, emptyTest);
        }

        public <V> Builder<T> addUnencodable(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                             @Nullable Predicate<V> emptyTest, String @Nullable ... altNames) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, null, emptyTest, altNames);
        }

        public <V> Builder<T> addUnencodableOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                @Nullable V defaultValue) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, () -> defaultValue, null, (String[]) null);
        }

        public <V> Builder<T> addUnencodableOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                @Nullable V defaultValue, String @Nullable ... altNames) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, () -> defaultValue, null, altNames);
        }

        public <V> Builder<T> addUnencodableOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                @Nullable V defaultValue, @Nullable Predicate<V> emptyTest) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, () -> defaultValue, emptyTest);
        }

        public <V> Builder<T> addUnencodableOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                @Nullable V defaultValue, @Nullable Predicate<V> emptyTest, String @Nullable ... altNames) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, () -> defaultValue, emptyTest, altNames);
        }

        public <V> Builder<T> addUnencodableDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                   @Nullable Supplier<V> defaultSupplier) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, defaultSupplier, null, (String[]) null);
        }

        public <V> Builder<T> addUnencodableDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                   @Nullable Supplier<V> defaultSupplier, String @Nullable ... altNames) {
            return addUnencodableDynOpt(name, fieldWriter, fieldReader, defaultSupplier, null, altNames);
        }

        public <V> Builder<T> addUnencodableDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                   @Nullable Supplier<V> defaultSupplier, @Nullable Predicate<V> emptyTest) {
            return addDynOpt(name, fieldWriter, fieldReader, null, defaultSupplier, emptyTest);
        }

        /**
         * Adds a new unencodable, optional, mutable property with a dynamic default value.
         * Unencodable means it cannot be converted to a data format like JSON. This is the case for functions.
         * These unencodable values are still important for copying and applying default values.
         *
         * @see #addDynOpt(String, FieldWriter, FieldReader, Codec, Supplier, Predicate)
         */
        public <V> Builder<T> addUnencodableDynOpt(String name, FieldWriter<T, V> fieldWriter, FieldReader<T, V> fieldReader,
                                                   @Nullable Supplier<V> defaultSupplier, @Nullable Predicate<V> emptyTest, String @Nullable ... altNames) {
            return addDynOpt(name, fieldWriter, fieldReader, null, defaultSupplier, emptyTest, altNames);
        }

        public Builder<T> registry(CodecRegistry<T> registry, String... names) {
            this.registry = registry;
            this.names = names;
            return this;
        }

        public Builder<T> registryTypeName(CodecRegistry<T> registry, String typeName) {
            return registry(registry, typeName, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, typeName));
        }

        public Builder<T> registryTypeName(CodecRegistry<T> registry, Class<T> typeName) {
            return registryTypeName(registry, typeName.getSimpleName());
        }

        public MutableObjectCodec<T> build() {
            var c = new MutableObjectCodec<>(this.fields, this.instanceDecoder, this.baseCopy);
            if (this.registry != null) {
                this.registry.register(c, this.names);
            }
            return c;
        }
    }

    public static class DrawableBuilder<T extends IDrawable> extends Builder<T> {

        @SuppressWarnings("unchecked")
        public DrawableBuilder(String typeName) {
            registryTypeName((CodecRegistry<T>) IDrawable.CODECS, typeName);
        }
    }

    @SuppressWarnings("unchecked")
    public static class WidgetBuilder<T extends IWidget> extends Builder<T> {

        public WidgetBuilder(String typeName) {
            registryTypeName((CodecRegistry<T>) IWidget.CODECS, typeName);
        }
    }
}
