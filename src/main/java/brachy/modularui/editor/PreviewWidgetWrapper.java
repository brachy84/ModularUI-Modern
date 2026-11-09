package brachy.modularui.editor;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.widget.RecursiveDelegatingWidget;

import com.mojang.blaze3d.platform.InputConstants;

import org.jetbrains.annotations.NotNull;

public class PreviewWidgetWrapper extends RecursiveDelegatingWidget<PreviewWidgetWrapper> implements Interactable {

    public static final IDrawable OUTLINE = new IDrawable() {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            GuiDraw.drawBorderOutsideXYWH(context.getGraphics(), x, y, width, height, 2f, Color.RED.main);
        }
    };

    public PreviewWidgetWrapper(IWidget delegate) {
        super(delegate);
    }

    @Override
    protected PreviewWidgetWrapper createChildDelegate(IWidget widget) {
        return new PreviewWidgetWrapper(widget);
    }

    public boolean isSelected() {
        return getScreen() instanceof EditorScreen editorScreen && editorScreen.getSelectedWidget() == this;
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (isSelected()) {
            context.graphicsPose().pushPose();
            context.applyTo(context.graphicsPose());
            OUTLINE.drawAtZero(context, getArea(), WidgetTheme.getDefault().theme());
            context.graphicsPose().popPose();
        }
        super.drawForeground(context);
    }


    @Override
    public @NotNull Result onMousePressed(int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (getScreen() instanceof EditorScreen editorScreen) {
                editorScreen.updateSelected(this);
            }
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }
}
