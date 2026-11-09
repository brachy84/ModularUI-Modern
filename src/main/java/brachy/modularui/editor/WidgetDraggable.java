package brachy.modularui.editor;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IParentWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.SingleChildWidget;
import brachy.modularui.widget.WidgetType;
import brachy.modularui.widgets.draggable.AbstractDraggable;

public class WidgetDraggable extends AbstractDraggable {

    private final WidgetType<?> type;
    private final IDrawable icon;

    public WidgetDraggable(WidgetType<?> type) {
        this.type = type;
        this.icon = Text.str(type.name());
        getMovingArea().setSize(60, 60);
    }

    @Override
    public void drawMovingState(ModularGuiContext context, float partialTicks) {
        this.icon.draw(context, getMovingArea(), WidgetTheme.getDefault().theme());
    }

    @Override
    public void onDragEnd(ModularGuiContext context) {
        var hovered = context.getTopHovered();
        if (hovered instanceof PreviewWidgetWrapper ww) {
            if (ww.getDelegate() instanceof IParentWidget<?, ?> parent) {
                parent.addChildRaw(this.type.createNewInstance(), -1);
                if (ww.getScreen() instanceof EditorScreen editorScreen) {
                    editorScreen.buildWidgetTreeView();
                }
            } else if (ww.getDelegate() instanceof SingleChildWidget<?> singleChildWidget) {
                singleChildWidget.child(this.type.createNewInstance());
                if (ww.getScreen() instanceof EditorScreen editorScreen) {
                    editorScreen.buildWidgetTreeView();
                }
            }
        }
    }
}
