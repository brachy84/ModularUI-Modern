package brachy.modularui.api.codec;

import com.mojang.serialization.Encoder;

import org.jetbrains.annotations.Nullable;

public interface ExtendedMutableCodec<A> extends MutableCodec<A>, IExtendedCodec<A> {

    default ExtendedMutableCodec<A> tryBefore(MutableCodec<A> codec) {
        return tryBefore(codec, codec);
    }

    default ExtendedMutableCodec<A> tryBeforeDecoder(MutableDecoder<A> decoder) {
        return tryBefore(decoder, null);
    }

    default ExtendedMutableCodec<A> tryBeforeEncoder(Encoder<A> encoder) {
        return tryBefore(null, encoder);
    }

    default ExtendedMutableCodec<A> tryBefore(@Nullable MutableDecoder<A> decoder, @Nullable Encoder<A> encoder) {
        return new MutableCodec.Chained<>(decoder, encoder, this, this);
    }
}
