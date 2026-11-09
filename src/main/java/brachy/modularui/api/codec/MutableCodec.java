package brachy.modularui.api.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link Codec} that can decode mutable field to an existing instance.
 *
 * @param <A> type of instance
 */
public interface MutableCodec<A> extends Codec<A>, MutableDecoder<A> {

    default DataResult<Pair<A, JsonElement>> decodeJson(JsonObject json) {
        return decode(JsonOps.INSTANCE, json);
    }

    default DataResult<A> parseJson(JsonObject json) {
        return parse(JsonOps.INSTANCE, json);
    }

    default DataResult<Pair<A, JsonElement>> decodeJson(JsonObject json, A instance) {
        return decode(JsonOps.INSTANCE, json, instance);
    }

    default DataResult<A> parseJson(JsonObject json, A instance) {
        return parse(JsonOps.INSTANCE, json, instance);
    }

    default DataResult<JsonElement> encodeJson(A instance) {
        return encodeStart(JsonOps.INSTANCE, instance);
    }

    default DataResult<JsonElement> encodeJson(A instance, JsonElement prefix) {
        return encode(instance, JsonOps.INSTANCE, prefix);
    }

    static <A> MutableCodec<A> of(Encoder<A> encoder, MutableDecoder<A> decoder) {
        return of(encoder, decoder, decoder);
    }

    static <A> MutableCodec<A> of(Encoder<A> encoder, MutableDecoder<A> decoder, String name) {
        return of(encoder, decoder, decoder, name);
    }

    static <A> MutableCodec<A> of(Encoder<A> encoder, MutableDecoder<A> decoder, InstanceDecoder<A> instanceDecoder) {
        return of(encoder, decoder, instanceDecoder, "MutableCodec[" + encoder + " " + decoder + "]");
    }

    static <A> MutableCodec<A> of(Encoder<A> encoder, MutableDecoder<A> decoder, InstanceDecoder<A> instanceDecoder, String name) {
        return new MutableCodec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input, A instance) {
                return decoder.decode(ops, input, instance);
            }

            @Override
            public <T> DataResult<Pair<A, T>> decodeInstance(DynamicOps<T> ops, T input) {
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
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    static <A, S> MutableCodec<S> xmapOf(Codec<A> codec,
                                         final BiConsumer<S, ? super A> to,
                                         final Function<? super S, ? extends A> from) {
        return xmapOf(codec, to, from, null);
    }

    static <A, S> MutableCodec<S> xmapOf(Codec<A> codec,
                                         final BiConsumer<S, ? super A> to,
                                         final Function<? super S, ? extends A> from,
                                         InstanceDecoder<S> instanceDecoder) {
        return MutableCodec.of(codec.comap(from), MutableDecoder.mapOf(codec, to, instanceDecoder), codec + "[xmapped]");
    }

    static <A, S> MutableCodec<S> comapFlatMapOf(Codec<A> codec,
                                                 final BiFunction<S, ? super A, DataResult<? extends S>> to,
                                                 final Function<? super S, ? extends A> from) {
        return comapFlatMapOf(codec, to, from, null);
    }

    static <A, S> MutableCodec<S> comapFlatMapOf(Codec<A> codec,
                                                 final BiFunction<S, ? super A, DataResult<? extends S>> to,
                                                 final Function<? super S, ? extends A> from, InstanceDecoder<S> instanceDecoder) {
        return MutableCodec.of(codec.comap(from), MutableDecoder.flatMapOf(codec, to, instanceDecoder), codec + "[comapFlatMapped]");
    }

    static <A, S> MutableCodec<S> flatComapMapOf(Codec<A> codec,
                                                 final BiConsumer<S, ? super A> to,
                                                 final Function<? super S, ? extends DataResult<? extends A>> from) {
        return flatComapMapOf(codec, to, from, null);
    }

    static <A, S> MutableCodec<S> flatComapMapOf(Codec<A> codec,
                                                 final BiConsumer<S, ? super A> to,
                                                 final Function<? super S, ? extends DataResult<? extends A>> from,
                                                 InstanceDecoder<S> instanceDecoder) {
        return MutableCodec.of(codec.flatComap(from), MutableDecoder.mapOf(codec, to, instanceDecoder), codec + "[flatComapMapped]");
    }

    static <A, S> MutableCodec<S> flatXmapOf(Codec<A> codec,
                                             final BiFunction<S, ? super A, DataResult<? extends S>> to,
                                             final Function<? super S, ? extends DataResult<? extends A>> from) {
        return flatXmapOf(codec, to, from, null);
    }

    static <A, S> MutableCodec<S> flatXmapOf(Codec<A> codec,
                                             final BiFunction<S, ? super A, DataResult<? extends S>> to,
                                             final Function<? super S, ? extends DataResult<? extends A>> from,
                                             InstanceDecoder<S> instanceDecoder) {
        return MutableCodec.of(codec.flatComap(from), MutableDecoder.flatMapOf(codec, to, instanceDecoder), codec + "[flatXmapped]");
    }

    class Chained<A> implements ExtendedMutableCodec<A> {

        private final MutableDecoder<A> beforeDecoder;
        private final Encoder<A> beforeEncoder;
        private final IExtendedCodec<A> extendedCodec;
        private final MutableCodec<A> afterCodec;

        public Chained(MutableCodec<A> beforeCodec, IExtendedCodec<A> extendedCodec, MutableCodec<A> afterCodec) {
            this(beforeCodec, beforeCodec, extendedCodec, afterCodec);
        }

        public Chained(@Nullable MutableDecoder<A> beforeDecoder, @Nullable Encoder<A> beforeEncoder,
                       IExtendedCodec<A> extendedCodec, MutableCodec<A> afterCodec) {
            this.beforeDecoder = beforeDecoder;
            this.beforeEncoder = beforeEncoder;
            this.extendedCodec = extendedCodec;
            this.afterCodec = afterCodec;
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
        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input, A instance) {
            if (this.beforeDecoder != null) {
                var d = this.beforeDecoder.decode(ops, input, instance);
                var res = d.result();
                if (res.isPresent()) return d;
            }
            return this.afterCodec.decode(ops, input, instance);
        }

        @Override
        public <T> DataResult<Pair<A, T>> decodeInstance(DynamicOps<T> ops, T input) {
            return this.afterCodec.decodeInstance(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
            if (this.beforeEncoder != null) {
                var d = this.beforeEncoder.encode(input, ops, prefix);
                var res = d.result();
                if (res.isPresent()) return d;
            }
            return this.afterCodec.encode(input, ops, prefix);
        }
    }
}
