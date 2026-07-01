package brachy.modularui.widgets.draggable;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IDraggable;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.widget.sizer.Area;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractDraggable implements IDraggable {

    private boolean moving;
    private final Area area = new Area();

    @Override
    public boolean onDragStart(ModularGuiContext context, int button) {
        return true;
    }

    @Override
    public void onDrag(ModularGuiContext context, int mouseButton, double timeSinceLastClick) {
        this.area.x = context.getAbsMouseX() - this.area.width / 2;
        this.area.y = context.getAbsMouseY() - this.area.height / 2;
    }

    @Override
    public @NotNull Area getMovingArea() {
        return this.area;
    }

    @Override
    public boolean isMoving() {
        return this.moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    @Override
    public void transform(IViewportStack viewportStack) {}
}
