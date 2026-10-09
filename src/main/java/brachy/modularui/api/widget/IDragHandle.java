package brachy.modularui.api.widget;

import brachy.modularui.screen.viewport.ModularGuiContext;

import org.jetbrains.annotations.Nullable;

public interface IDragHandle {

    @Nullable IDraggable createDraggable(ModularGuiContext ctx, int button);
}
