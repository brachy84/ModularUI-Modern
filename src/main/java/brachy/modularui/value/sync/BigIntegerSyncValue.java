package brachy.modularui.value.sync;

import brachy.modularui.api.value.IStringValue;
import brachy.modularui.utils.ICopy;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigIntegerSyncValue extends GenericSyncValue<BigInteger> implements IStringValue<BigInteger> {

    public BigIntegerSyncValue(@NonNull Supplier<BigInteger> getter, @Nullable Consumer<BigInteger> setter) {
        this(getter, setter, false);
    }

    public BigIntegerSyncValue(@NonNull Supplier<BigInteger> getter, @Nullable Consumer<BigInteger> setter,
                               boolean nullable) {
        super(BigInteger.class, getter, setter, ByteBufAdapters.BIG_INT, ICopy.immutable(), nullable);
    }

    @Override
    public String getStringValue() {
        return getValue().toString();
    }

    @Override
    public void setStringValue(String val) {
        setValue(new BigInteger(val));
    }
}
