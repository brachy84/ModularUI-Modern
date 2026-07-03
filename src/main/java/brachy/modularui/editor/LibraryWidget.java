package brachy.modularui.editor;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IDragHandle;
import brachy.modularui.api.widget.IDraggable;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.widget.WidgetType;
import brachy.modularui.widgets.TextWidget;

import org.jetbrains.annotations.Nullable;

public class LibraryWidget extends TextWidget<LibraryWidget> implements IDragHandle {

    private final WidgetType<?> type;

    public LibraryWidget(WidgetType<?> type) {
        super(Text.str(type.name()).color(Color.WHITE.main));
        this.type = type;
        size(60);
        textAlign(Alignment.CENTER);
    }

    @Override
    public @Nullable IDraggable createDraggable(ModularGuiContext ctx, int button) {
        return new WidgetDraggable(this.type);
    }
}
