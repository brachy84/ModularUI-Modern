package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public static <E, A> Codec<E> dispatchNullable(Codec<A> keyCodec, Function<? super E, ? extends A> type, Function<? super A, ? extends Codec<? extends E>> codec) {
        return dispatchNullable("type", keyCodec, type, codec);
    }

    public static <E, A> Codec<E> dispatchNullable(String key, Codec<A> keyCodec, Function<? super E, ? extends A> type, Function<? super A, ? extends Codec<? extends E>> codec) {
        return keyCodec.partialDispatch(key, e -> {
            A a = type.apply(e);
            return a == null ? DataResult.error(() -> "No key found") : DataResult.success(a);
        }, a -> {
            Codec<? extends E> e = codec.apply(a);
            return e == null ? DataResult.error(() -> "No codec found for key " + a) : DataResult.success(e);
        });
    }

    public static <A> Encoder<A> checked(Encoder<A> codec, Predicate<A> test) {
        return new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return test.test(input) ? codec.encode(input, ops, prefix) : DataResult.error(() -> "Codec " + codec + " can't handle value " + input);
            }
        };
    }

    public static <A> Codec<A> checkedEncoder(Codec<A> codec, Predicate<A> test) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return codec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return test.test(input) ? codec.encode(input, ops, prefix) : DataResult.error(() -> "Codec " + codec + " can't handle value " + input);
            }

            @Override
            public String toString() {
                return super.toString() + "[Checked encoder]";
            }
        };
    }

    public static <T> @Nullable Stream.Builder<Pair<T, T>> mergePrefixToMapBuilder(DynamicOps<T> ops, @Nullable T prefix) {
        return mergePrefixToMapBuilder(ops, prefix, null);
    }

    public static <T> @Nullable Stream.Builder<Pair<T, T>> mergePrefixToMapBuilder(DynamicOps<T> ops, @Nullable T prefix, @Nullable Stream.Builder<Pair<T, T>> mapValues) {
        if (mapValues == null) mapValues = Stream.builder();
        if (prefix != null && !Objects.equals(prefix, ops.empty())) {
            // add values of prefix map
            // this is more performant than mergeToMap of DynamicOps
            var prefixMap = ops.getMapValues(prefix);
            var res = prefixMap.result();
            if (res.isEmpty()) return null; // prefix is not empty and is not a map
            res.get().forEach(mapValues);
        }
        return mapValues;
    }
}
