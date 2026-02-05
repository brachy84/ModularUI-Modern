package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IFloatValue;

public interface IFloatSyncValue<T> extends IValueSyncHandler<T>, IFloatValue<T> {

    @Override
    default void setFloatValue(float val) {
        setFloatValue(val, true, true);
    }

    default void setFloatValue(float val, boolean setSource) {
        setFloatValue(val, setSource, true);
    }

    void setFloatValue(float value, boolean setSource, boolean sync);
}
