package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

public class CodecUtil {

    @SafeVarargs
    public static <A> Codec<A> chainedCodec(Codec<A>... codecs) {
        if (codecs == null || codecs.length == 0) throw new NullPointerException();
        if (codecs.length == 1) return codecs[0];
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                StringBuilder message = new StringBuilder();
                DataResult<Pair<A, T>> last = null;
                for (var codec : codecs) {
                    last = codec.decode(ops, input);
                    if (last.result().isPresent()) return last;
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return last.mapError(s -> message.substring(0, message.length() - 2));
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                StringBuilder message = new StringBuilder();
                DataResult<T> last = null;
                for (var codec : codecs) {
                    last = codec.encode(input, ops, prefix);
                    if (last.result().isPresent()) return last;
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return last.mapError(s -> message.substring(0, message.length() - 2));
            }
        };
    }

    @SafeVarargs
    public static <A> Decoder<A> chainedDecoder(Decoder<A>... decoder) {
        if (decoder == null || decoder.length == 0) throw new NullPointerException();
        if (decoder.length == 1) return decoder[0];
        return new Decoder<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                StringBuilder message = new StringBuilder();
                DataResult<Pair<A, T>> last = null;
                for (var codec : decoder) {
                    last = codec.decode(ops, input);
                    if (last.result().isPresent()) return last;
                    message.append(last.error().orElseThrow().message()).append("; ");
                }
                return last.mapError(s -> message.substring(0, message.length() - 2));
            }
        };
    }

    @SafeVarargs
    public static <A> Codec<A> codecOf(Encoder<A> encoder, Decoder<A>... decoder) {
        return Codec.of(encoder, chainedDecoder(decoder));
    }
}
