package brachy.modularui.utils.serialization.codec;

import brachy.modularui.api.codec.MutableCodec;
import brachy.modularui.api.codec.MutableMapDecoder;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

public abstract class MutableMapCodec<A> extends MapCodec<A> implements MutableMapDecoder<A> {

    public static <A> @Nullable MutableMapCodec<A> getFrom(Codec<A> codec) {
        if (codec instanceof MapCodec.MapCodecCodec<A> mcc && mcc.codec() instanceof MutableMapCodec<A> mmc) return mmc;
        if (codec instanceof MutableMapCodec.MutableCodecCodec<A> mcc) return mcc.codec();
        return null;
    }

    private final MapCodec.MapCodecCodec<A> codec = new MapCodecCodec<>(this);
    private final MutableCodecCodec<A> mutableCodec = new MutableCodecCodec<>(this);

    @Override
    public Codec<A> codec() {
        return this.codec;
    }

    public MutableCodecCodec<A> mutableCodec() {
        return this.mutableCodec;
    }

    public record MutableCodecCodec<A>(MutableMapCodec<A> codec) implements MutableCodec<A> {

        @Override
        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input, A instance) {
            return CodecUtil.ifMap(ops, input, map -> this.codec.decode(ops, map, instance)).map(t -> new Pair<>(t, input));
        }

        @Override
        public <T> DataResult<Pair<A, T>> decodeInstance(DynamicOps<T> ops, T input) {
            return CodecUtil.ifMap(ops, input, map -> this.codec.decodeInstance(ops, map)).map(t -> new Pair<>(t, input));
        }

        @Override
        public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
            return this.codec.codec().encode(input, ops, prefix);
        }
    }
}
