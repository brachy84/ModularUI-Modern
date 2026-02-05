package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.widget.IWidget;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

public interface IDynamicSyncNotifiable {

    /**
     * An internal function which is used to link the {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     */
    @ApiStatus.Internal
    void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate);
}
