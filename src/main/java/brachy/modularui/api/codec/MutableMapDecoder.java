package brachy.modularui.api.codec;

import brachy.modularui.utils.serialization.codec.MutableMapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;

import org.jetbrains.annotations.Nullable;

public interface MutableMapDecoder<A> extends MapDecoder<A>, InstanceMapDecoder<A> {

    static <A> @Nullable MutableMapDecoder<A> getFrom(Codec<A> codec) {
        if (codec instanceof MapCodec.MapCodecCodec<A> mcc && mcc.codec() instanceof MutableMapDecoder<?> mmc) return (MutableMapDecoder<A>) mmc;
        if (codec instanceof MutableMapCodec.MutableCodecCodec<A> mcc) return mcc.codec();
        return null;
    }

    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input, A instance);

    @Override
    default <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
        var d = decodeInstance(ops, input);
        var result = d.result();
        if (result.isEmpty()) return d;
        return decode(ops, input, result.get());
    }
}
