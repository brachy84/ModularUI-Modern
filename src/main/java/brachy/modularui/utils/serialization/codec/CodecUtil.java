package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        return dispatchNullable("type", keyCodec, type, codec, true);
    }

    /**
     * Creates a dispatch codec, but with nullable type and codec functions.
     * If the functions return null, an error data result is returned instead of crashing.
     *
     * @see #dispatch(String, Codec, Function, Function, boolean)
     */
    public static <K, V> Codec<V> dispatchNullable(String key, Codec<K> keyCodec,
                                                   Function<? super V, ? extends K> type,
                                                   Function<? super K, ? extends Codec<? extends V>> codec, boolean assumeMap) {
        return dispatch(key, keyCodec, v -> {
            K k = type.apply(v);
            return k == null ? DataResult.error(() -> "No key found") : DataResult.success(k);
        }, k -> {
            Codec<? extends V> e = codec.apply(k);
            return e == null ? DataResult.error(() -> "No codec found for key " + k) : DataResult.success(e);
        }, assumeMap);
    }

    /**
     * Creates a dispatch codec with the option to assume map.
     * {@link Codec#dispatch(Function, Function)} assumes the data in a structure like this for this example:
     * <pre>
     * {@code
     *     {
     *         "type": "pos2d",
     *         "value: {
     *             "x": 1,
     *             "y": 2
     *         }
     *     }
     * }
     * </pre>
     * With assumeMap it would look like this:
     * <pre>
     * {@code
     *     {
     *         "type": "pos2d",
     *         "x": 1,
     *         "y": 2
     *     }
     * }
     * </pre>
     *
     * @param key       key to get the type name, usually just "type"
     * @param keyCodec  codec for the key
     * @param type      function to get the key from a value
     * @param codec     function to get the codec from a key
     * @param assumeMap if the codec should assume map like described above
     * @param <K>       key type
     * @param <V>       value type
     */
    public static <K, V> Codec<V> dispatch(String key, Codec<K> keyCodec,
                                           Function<? super V, ? extends DataResult<? extends K>> type,
                                           Function<? super K, ? extends DataResult<? extends Codec<? extends V>>> codec, boolean assumeMap) {
        return assumeMap ?
                KeyDispatchCodec.unsafe(key, keyCodec, type, codec, v -> CodecUtil.getCodec(type, codec, v)).codec() :
                new KeyDispatchCodec<>(key, keyCodec, type, codec).codec();

    }

    @SuppressWarnings("unchecked")
    private static <K, V> DataResult<? extends Encoder<V>> getCodec(final Function<? super V, ? extends DataResult<? extends K>> type,
                                                                    final Function<? super K, ? extends DataResult<? extends Encoder<? extends V>>> encoder,
                                                                    final V input) {
        return type.apply(input)
                .<Encoder<? extends V>>flatMap(k -> encoder.apply(k).map(Function.identity()))
                .map(c -> ((Encoder<V>) c));
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

    public static <A> Codec<Set<A>> setOf(Codec<A> codec) {
        return codec.listOf().xmap(ObjectOpenHashSet::new, ArrayList::new);
    }

    /**
     * Creates a codec that accepts either a data list or a single element and turns it into a list.
     */
    public static <A> Codec<List<A>> listLike(Codec<A> codec) {
        return chainedCodec(codec.flatComapMap(Collections::singletonList, list -> {
            if (list.size() != 1) return DataResult.error(() -> "List must contain exactly one element");
            return DataResult.success(list.get(0));
        }), codec.listOf());
    }

    /**
     * Creates a codec that accepts either a data list or a single element and turns it into a set.
     */
    public static <A> Codec<Set<A>> setLike(Codec<A> codec) {
        return chainedCodec(codec.flatComapMap(Collections::singleton, list -> {
            if (list.size() != 1) return DataResult.error(() -> "List must contain exactly one element");
            return DataResult.success(list.iterator().next());
        }), setOf(codec));
    }
}
