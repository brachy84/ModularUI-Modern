package brachy.modularui.api.codec;

import brachy.modularui.utils.serialization.codec.MutableMapCodec;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Allows decoded data to be written to an existing instance.
 *
 * @param <A> type of instance
 */
public interface MutableDecoder<A> extends Decoder<A>, InstanceDecoder<A> {

    static <A> MutableDecoder<A> getFrom(Codec<A> codec) {
        if (codec instanceof MutableDecoder<?> decoder) return (MutableDecoder<A>) decoder;
        MutableMapCodec<A> mmc = MutableMapCodec.getFrom(codec);
        return mmc != null ? mmc.mutableCodec() : null;
    }

    <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input, A instance);

    @Override
    default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        var d = decodeInstance(ops, input);
        var result = d.result();
        if (result.isEmpty()) return d;
        return decode(ops, input, result.get().getFirst());
    }

    default <T> DataResult<A> parse(final DynamicOps<T> ops, final T input, A instance) {
        return decode(ops, input, instance).map(Pair::getFirst);
    }

    default <T> DataResult<Pair<A, T>> decode(final Dynamic<T> input, A instance) {
        return decode(input.getOps(), input.getValue(), instance);
    }

    default <T> DataResult<A> parse(final Dynamic<T> input, A instance) {
        return decode(input, instance).map(Pair::getFirst);
    }

    static <A, B> MutableDecoder<B> mapOf(Decoder<A> decoder, final BiConsumer<B, ? super A> function, InstanceDecoder<B> instanceDecoder) {
        return new MutableDecoder<>() {
            @Override
            public <T> DataResult<Pair<B, T>> decode(DynamicOps<T> ops, T input, B instance) {
                return decoder.decode(ops, input).map(p -> {
                    function.accept(instance, p.getFirst());
                    return Pair.of(instance, p.getSecond());
                });
            }

            @Override
            public <T> DataResult<Pair<B, T>> decodeInstance(DynamicOps<T> ops, T input) {
                if (!canDecodeInstance()) {
                    throw new IllegalStateException("Can't decode instance");
                }
                return instanceDecoder.decodeInstance(ops, input);
            }

            @Override
            public boolean canDecodeInstance() {
                return instanceDecoder != null && instanceDecoder.canDecodeInstance();
            }

            @Override
            public String toString() {
                return decoder.toString() + "[mapped]";
            }
        };
    }

    static <A, B> MutableDecoder<B> flatMapOf(Decoder<A> decoder, final BiFunction<B, ? super A, DataResult<? extends B>> function, InstanceDecoder<B> instanceDecoder) {
        return new MutableDecoder<>() {
            @Override
            public <T> DataResult<Pair<B, T>> decode(DynamicOps<T> ops, T input, B instance) {
                return decoder.decode(ops, input).flatMap(p -> function.apply(instance, p.getFirst()).map(b -> Pair.of(b, p.getSecond())));
            }

            @Override
            public <T> DataResult<Pair<B, T>> decodeInstance(DynamicOps<T> ops, T input) {
                if (!canDecodeInstance()) {
                    throw new IllegalStateException("Can't decode instance");
                }
                return instanceDecoder.decodeInstance(ops, input);
            }

            @Override
            public boolean canDecodeInstance() {
                return instanceDecoder != null && instanceDecoder.canDecodeInstance();
            }

            @Override
            public String toString() {
                return decoder.toString() + "[mapped]";
            }
        };
    }
}
