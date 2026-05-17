package brachy.modularui.utils.serialization.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

/**
 * Creates an instance and only serializes any immutable fields.
 * For no arg constructors this can just be a supplier.
 *
 * @param <A> type of instance
 */
public interface InstanceDecoder<A> {

    <T> DataResult<Pair<A, T>> decodeInstance(final DynamicOps<T> ops, final T input);
}
