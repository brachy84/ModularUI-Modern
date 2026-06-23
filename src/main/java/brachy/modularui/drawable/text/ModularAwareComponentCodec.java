package brachy.modularui.drawable.text;

import net.minecraft.network.chat.Component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps the vanilla {@link net.minecraft.network.chat.ComponentSerialization#CODEC component codec} so that the extra
 * {@link ModularComponent} properties (alignment, scale, shadow, ...) are preserved when a component tree is (de)serialized
 * by vanilla (tooltips, networking, commands, ...).
 * <p>
 * Since this wraps the <i>recursive</i> component codec, it is applied at every node of the tree, so nested modular
 * components are handled too. This replaces the old {@code ComponentSerializerMixin} which relied on the Gson based
 * {@code Component.Serializer} recursively calling itself - a mechanism that no longer exists since serialization moved
 * to codecs in 1.20.2+.
 */
public final class ModularAwareComponentCodec implements Codec<Component> {

    private final Codec<Component> delegate;

    public ModularAwareComponentCodec(Codec<Component> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> ops, T input) {
        return this.delegate.decode(ops, input).flatMap(pair -> {
            Optional<MapLike<T>> map = ops.getMap(input).result();
            if (map.isEmpty() || !hasModularField(map.get())) {
                return DataResult.success(pair);
            }
            ModularComponent mc = ModularComponent.of(pair.getFirst());
            return ModularComponent.CODEC.decode(ops, map.get(), mc)
                    .map(comp -> Pair.of((Component) comp, pair.getSecond()));
        });
    }

    @Override
    public <T> DataResult<T> encode(Component input, DynamicOps<T> ops, T prefix) {
        DataResult<T> result = this.delegate.encode(input, ops, prefix);
        if (!(input instanceof ModularComponent mc)) return result;
        // ModularComponent#tryCollapseToString returns null, so the delegate always encodes a map we can extend here
        return result.flatMap(encoded -> {
            RecordBuilder<T> builder = ops.mapBuilder();
            ModularComponent.CODEC.forEachField(field -> field.encode(mc, ops, builder));
            return builder.build(encoded);
        });
    }

    private static <T> boolean hasModularField(MapLike<T> map) {
        AtomicBoolean found = new AtomicBoolean(false);
        ModularComponent.CODEC.forEachField(field -> {
            if (found.get()) return;
            if (map.get(field.name()) != null) {
                found.set(true);
                return;
            }
            if (field.altNames() != null) {
                for (String alt : field.altNames()) {
                    if (map.get(alt) != null) {
                        found.set(true);
                        return;
                    }
                }
            }
        });
        return found.get();
    }
}
