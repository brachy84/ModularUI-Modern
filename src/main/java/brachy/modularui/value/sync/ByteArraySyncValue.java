package brachy.modularui.value.sync;

import brachy.modularui.utils.serialization.network.ByteBufAdapters;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ByteArraySyncValue extends GenericSyncValue<byte[]> {

    public ByteArraySyncValue(@NonNull Supplier<byte[]> getter, @Nullable Consumer<byte[]> setter) {
        this(getter, setter, false);
    }

    public ByteArraySyncValue(@NonNull Supplier<byte[]> getter, @Nullable Consumer<byte[]> setter, boolean nullable) {
        super(byte[].class, getter, setter, ByteBufAdapters.BYTE_ARR, byte[]::clone, nullable);
    }
}
