package brachy.modularui.widgets.draggable;

import brachy.modularui.api.layout.IViewport;
import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IDraggable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.DraggablePanelWrapper;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.HoveredWidgetList;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.Area;

import org.jetbrains.annotations.Nullable;

public class ProxyDraggable extends Widget<ProxyDraggable> implements IDraggable, IViewport {

    private IDraggable parentDraggable;

    @Override
    public void onInit() {
        IWidget parent = getParent();
        while (!(parent instanceof ModularPanel)) {
            if (parent instanceof IDraggable draggable) {
                this.parentDraggable = draggable;
                return;
            }
            parent = parent.getParent();
        }
        if (((ModularPanel<?>) parent).isDraggable()) {
            this.parentDraggable = new DraggablePanelWrapper((ModularPanel<?>) parent);
        }
    }

    @Override
    public void drawMovingState(ModularGuiContext context, float partialTicks) {
        if (this.parentDraggable != null) {
            this.parentDraggable.drawMovingState(context, partialTicks);
        }
    }

    @Override
    public boolean onDragStart(ModularGuiContext context, int button) {
        return this.parentDraggable != null && this.parentDraggable.onDragStart(context, button);
    }

    @Override
    public void onDragEnd(ModularGuiContext context) {
        if (this.parentDraggable != null) {
            this.parentDraggable.onDragEnd(context);
        }
    }

    @Override
    public void onDrag(ModularGuiContext context, int mouseButton, double timeSinceLastClick) {
        if (this.parentDraggable != null) {
            this.parentDraggable.onDrag(context, mouseButton, timeSinceLastClick);
        }
    }

    @Override
    public @Nullable Area getMovingArea() {
        Area.SHARED.reset();
        return this.parentDraggable != null ? this.parentDraggable.getMovingArea() : Area.SHARED;
    }

    @Override
    public boolean isMoving() {
        return this.parentDraggable != null && this.parentDraggable.isMoving();
    }

    @Override
    public void setMoving(boolean moving) {
        if (this.parentDraggable != null) {
            this.parentDraggable.setMoving(moving);
        }
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        if (this.parentDraggable instanceof IViewport viewport) {
            viewport.transformChildren(stack);
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (this.parentDraggable instanceof IViewport viewport) {
            viewport.getWidgetsAt(stack, widgets, x, y);
        } else {
            IViewport.super.getWidgetsAt(stack, widgets, x, y);
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (this.parentDraggable instanceof IViewport viewport) {
            viewport.getSelfAt(stack, widgets, x, y);
        } else {
            IViewport.super.getSelfAt(stack, widgets, x, y);
        }
    }
}
