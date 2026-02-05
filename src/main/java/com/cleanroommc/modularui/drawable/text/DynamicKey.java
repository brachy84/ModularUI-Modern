package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class DynamicKey extends BaseKey {

    private final Supplier<IKey> supplier;

    public DynamicKey(Supplier<IKey> supplier) {
        // Objects.requireNonNull(supplier.get(), "IKey returns a null string!");
        this.supplier = supplier;
    }

    @Override
    public MutableComponent get() {
        return toString(false, null);
    }

    @Override
    public MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        // formatting is prepended to each key
        return toString(true, parentFormatting);
    }

    private MutableComponent toString(boolean formatted, @Nullable FormattingState parentFormatting) {
        IKey key = this.supplier.get();
        if (key == null) key = IKey.EMPTY;
        if (formatted) {
            // merge parent formatting and this formatting to no lose info
            return key.getFormatted(FormattingState.merge(parentFormatting, getFormatting()));
        } else {
            return key.get();
        }
    }
}
