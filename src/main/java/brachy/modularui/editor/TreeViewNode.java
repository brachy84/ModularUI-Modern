package brachy.modularui.editor;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Color;
import brachy.modularui.widgets.TextWidget;

import org.jetbrains.annotations.NotNull;

public class TreeViewNode extends TextWidget<TreeViewNode> implements Interactable {

    private final PreviewWidgetWrapper widget;

    public TreeViewNode(PreviewWidgetWrapper wrapper) {
        super(Text.str(wrapper.getDelegate().getTypeName()));
        this.widget = wrapper;
        color(Color.WHITE.main);
        padding(2);
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (this.widget.isSelected()) {
            var a = getArea();
            GuiDraw.drawRect(context.getGraphics(), 0, 0, a.width, a.height, Color.withAlpha(Color.WHITE.main, 0.2f));
        }
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        return this.widget.onMousePressed(button);
    }
}
