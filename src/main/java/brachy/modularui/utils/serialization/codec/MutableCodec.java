package brachy.modularui.utils.serialization.codec;

import com.mojang.serialization.Codec;

/**
 * A {@link Codec} that can decode mutable field to an existing instance.
 *
 * @param <A> type of instance
 */
public interface MutableCodec<A> extends Codec<A>, MutableDecoder<A> {}
