package brachy.modularui.utils.serialization.codec;

import brachy.modularui.api.codec.ExtendedMutableCodec;
import brachy.modularui.api.codec.IExtendedCodec;
import brachy.modularui.api.codec.MutableCodec;
import brachy.modularui.api.codec.MutableDecoder;
import brachy.modularui.api.codec.MutableMapDecoder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public abstract class ExtendedMutableMapCodec<A> extends MutableMapCodec<A> implements IExtendedCodec<A> {

    public static <A> @Nullable ExtendedMutableMapCodec<A> getFrom(Codec<A> codec) {
        var c = MutableMapCodec.getFrom(codec);
        return c instanceof ExtendedMutableMapCodec<A> e ? e : null;
    }

    public ExtendedMutableMapCodec<A> tryBefore(MutableMapCodec<A> codec) {
        return tryBefore(codec, codec);
    }

    public ExtendedMutableMapCodec<A> tryBeforeDecoder(MutableMapDecoder<A> decoder) {
        return tryBefore(decoder, null);
    }

    public ExtendedMutableMapCodec<A> tryBeforeEncoder(MapEncoder<A> encoder) {
        return tryBefore(null, encoder);
    }

    public ExtendedMutableMapCodec<A> tryBefore(@Nullable MutableMapDecoder<A> decoder, @Nullable MapEncoder<A> encoder) {
        return decoder != null || encoder != null ? new Chained<>(decoder, encoder, this) : this;
    }

    public ExtendedMutableCodec<A> tryBefore(MutableCodec<A> codec) {
        return tryBefore(codec, codec);
    }

    public ExtendedMutableCodec<A> tryBeforeDecoder(MutableDecoder<A> decoder) {
        return tryBefore(decoder, null);
    }

    public ExtendedMutableCodec<A> tryBeforeEncoder(Encoder<A> encoder) {
        return tryBefore(null, encoder);
    }

    public ExtendedMutableCodec<A> tryBefore(@Nullable MutableDecoder<A> decoder, @Nullable Encoder<A> encoder) {
        return new MutableCodec.Chained<>(decoder, encoder, this, mutableCodec());
    }

    private static class Chained<A> extends ExtendedMutableMapCodec<A> {

        private final MutableMapDecoder<A> beforeDecoder;
        private final MapEncoder<A> beforeEncoder;
        private final ExtendedMutableMapCodec<A> extendedCodec;

        public Chained(MutableMapDecoder<A> beforeDecoder, MapEncoder<A> beforeEncoder, ExtendedMutableMapCodec<A> extendedCodec) {
            this.beforeDecoder = beforeDecoder;
            this.beforeEncoder = beforeEncoder;
            this.extendedCodec = extendedCodec;
        }

        @Override
        public A copy(A instance) {
            return this.extendedCodec.copy(instance);
        }

        @Override
        public <B extends A> B copyFields(A from, B to) {
            return this.extendedCodec.copyFields(from, to);
        }

        @Override
        public String convertToString(A a, int indent) {
            return this.extendedCodec.convertToString(a, indent);
        }

        @Override
        public boolean areEqual(@NotNull A t1, @NotNull A t2) {
            return this.extendedCodec.areEqual(t1, t2);
        }

        @Override
        public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input, A instance) {
            if (this.beforeDecoder != null) {
                var d = this.beforeDecoder.decode(ops, input, instance);
                var res = d.result();
                if (res.isPresent()) return d;
            }
            return this.extendedCodec.decode(ops, input, instance);
        }

        @Override
        public <T> DataResult<A> decodeInstance(DynamicOps<T> ops, MapLike<T> input) {
            return this.extendedCodec.decodeInstance(ops, input);
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return this.extendedCodec.keys(ops);
        }

        @Override
        public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (this.beforeEncoder != null) {
                var testBuilder = ops.mapBuilder();
                testBuilder = this.beforeEncoder.encode(input, ops, testBuilder);
                var d = testBuilder.build(ops.empty());
                var res = d.result();
                if (res.isPresent()) {
                    return this.beforeEncoder.encode(input, ops, prefix);
                }
            }
            return this.extendedCodec.encode(input, ops, prefix);
        }
    }
}
