package brachy.modularui.value.sync;

import brachy.modularui.utils.serialization.network.ByteBufAdapters;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LongArraySyncValue extends GenericSyncValue<long[]> {

    public LongArraySyncValue(@NonNull Supplier<long[]> getter, @Nullable Consumer<long[]> setter) {
        this(getter, setter, false);
    }

    public LongArraySyncValue(@NonNull Supplier<long[]> getter, @Nullable Consumer<long[]> setter, boolean nullable) {
        super(long[].class, getter, setter, ByteBufAdapters.LONG_ARR, long[]::clone, nullable);
    }
}
