package brachy.modularui.widgets.dynamic;

import brachy.modularui.api.widget.IWidget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public interface IDynamicHandler {

    @ApiStatus.Internal
    void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate);
}
