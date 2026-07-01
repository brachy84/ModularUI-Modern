package brachy.modularui.utils.serialization.codec;

import com.google.common.collect.Iterators;

import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import com.google.common.base.CharMatcher;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class CodecRegistry<T, E extends CodecRegistry.Entry<? extends T>> implements Iterable<E> {

    private static final CharMatcher DISALLOWED_NAME_CHARS = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.anyOf("_:"))
            .negate();

    private final Map<String, E> types = new Object2ObjectOpenHashMap<>();
    private final Codec<E> codec = ExtraCodecs.stringResolverCodec(e -> e == null ? "null" : e.name(), this::getNullable);

    public @Nullable E getNullable(String name) {
        return this.types.get(name);
    }

    public Optional<E> getOptional(String name) {
        return Optional.ofNullable(getNullable(name));
    }

    public @Nullable MapCodec<? extends T> getNullableMapCodec(String name) {
        var t = this.types.get(name);
        return t == null ? null : t.codec();
    }

    public @Nullable Codec<? extends T> getNullableCodec(String name) {
        var t = this.types.get(name);
        return t == null ? null : t.codec().codec();
    }

    public Codec<E> byNameCodec() {
        return this.codec;
    }

    protected void register(E entry) {
        if (this.types.containsKey(entry.name())) {
            throw new IllegalArgumentException("An entry with name '" + entry.name() + "' already exists!");
        }
        if (DISALLOWED_NAME_CHARS.matchesAnyOf(entry.name())) {
            throw new IllegalArgumentException("Entry new can only contain lower case letters, underscores and colons.");
        }
        this.types.put(entry.name(), entry);
    }

    @Unmodifiable
    @Override
    public @NotNull Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(this.types.values().iterator());
    }

    public static class Entry<T> {

        private final String name;
        private final MapCodec<T> codec;

        protected Entry(String name, MapCodec<T> codec) {
            this.name = name;
            this.codec = codec;
        }

        public String name() {
            return name;
        }

        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Entry<?>) obj;
            return Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return "Type[" +
                    "name=" + name + ']';
        }
    }
}
