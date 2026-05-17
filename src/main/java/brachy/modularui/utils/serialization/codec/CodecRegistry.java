package brachy.modularui.utils.serialization.codec;

import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CodecRegistry<T> {

    private final Map<String, Codec<? extends T>> drawableCodecs = new Object2ReferenceOpenHashMap<>();

    public @Nullable Codec<? extends T> getNullable(String name) {
        return this.drawableCodecs.get(name);
    }

    public @Nullable Codec<? extends T> get(String name) {
        return Objects.requireNonNull(getNullable(name));
    }

    public Optional<Codec<? extends T>> getOptional(String name) {
        return Optional.ofNullable(getNullable(name));
    }

    public Codec<? extends T> getOrElse(String name, Codec<? extends T> codec) {
        return this.drawableCodecs.getOrDefault(name, codec);
    }

    public <A extends T> Codec<A> register(String name, Codec<A> codec) {
        this.drawableCodecs.put(name, Objects.requireNonNull(codec));
        return codec;
    }

    public <A extends T> Codec<A> register(Codec<A> codec, String... names) {
        for (String name : names) this.drawableCodecs.put(name, codec);
        return codec;
    }
}
