package brachy.modularui.value;

import brachy.modularui.api.value.IValue;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ObjectValue<T> implements IValue<T> {

    public static <T> Dynamic<T> wrap(IValue<T> val) {
        return new Dynamic<>(val::getValue, val::setValue);
    }

    public static <T> Dynamic<T> wrapAtomic(AtomicReference<T> val) {
        return new Dynamic<>(val::get, val::set);
    }

    private final Class<T> type;
    private T value;

    public ObjectValue(Class<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    @Deprecated
    public ObjectValue(T value) {
        this.type = value != null ? (Class<T>) value.getClass() : null;
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public Class<T> getValueType() {
        return this.type != null ? this.type : (Class<T>) this.value.getClass();
    }

    public static class Dynamic<T> implements IValue<T> {

        private final Class<T> type;
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        public static <T> Dynamic<T> nullSafe(Class<T> type, Supplier<T> getter, Consumer<T> setter, Supplier<T> defaultValue, @Nullable Predicate<T> toNull) {
            return new Dynamic<>(type, () -> {
                var t = getter.get();
                return t == null ? defaultValue.get() : t;
            }, setter != null && toNull != null ? v -> {
                setter.accept(toNull.test(v) ? null : v);
            } : setter);
        }

        public Dynamic(Class<T> type, Supplier<T> getter, Consumer<T> setter) {
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }

        @Deprecated
        public Dynamic(Supplier<T> getter, Consumer<T> setter) {
            this.getter = getter;
            this.setter = setter;
            T value = getter.get();
            this.type = value != null ? (Class<T>) value.getClass() : null;
        }

        @Override
        public T getValue() {
            return this.getter.get();
        }

        @Override
        public void setValue(T value) {
            this.setter.accept(value);
        }

        @Override
        public Class<T> getValueType() {
            return this.type != null ? this.type : (Class<T>) this.getter.get().getClass();
        }
    }
}
