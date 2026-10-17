package brachy.modularui.value.sync;

import brachy.modularui.api.value.IStringValue;
import brachy.modularui.utils.ICopy;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigDecimalSyncValue extends GenericSyncValue<BigDecimal> implements IStringValue<BigDecimal> {

    public BigDecimalSyncValue(@NonNull Supplier<BigDecimal> getter, @Nullable Consumer<BigDecimal> setter) {
        this(getter, setter, false);
    }

    public BigDecimalSyncValue(@NonNull Supplier<BigDecimal> getter, @Nullable Consumer<BigDecimal> setter,
                               boolean nullable) {
        super(BigDecimal.class, getter, setter, ByteBufAdapters.BIG_DECIMAL, ICopy.immutable(), nullable);
    }

    @Override
    public String getStringValue() {
        return getValue().toString();
    }

    @Override
    public void setStringValue(String val) {
        setValue(new BigDecimal(val));
    }
}
