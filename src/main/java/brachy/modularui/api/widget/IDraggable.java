package brachy.modularui.api.widget;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.draggable.DraggableWidget;

import org.jetbrains.annotations.Nullable;

/**
 * Marks a widget as draggable.
 * The dragging is handled by ModularUI.
 *
 * @see DraggableWidget
 */
public interface IDraggable extends IDragHandle {

    /**
     * Gets called every frame after everything else is rendered.
     * Is only called when {@link #isMoving()} is true.
     * Translate to the mouse pos and draw with {@link WidgetTree#drawTree(IWidget, ModularGuiContext, boolean, boolean)} if it's a widget.
     *
     * @param partialTicks difference from last from
     */
    void drawMovingState(ModularGuiContext context, float partialTicks);

    /**
     * @param button  the mouse button that's holding down
     * @return false if the action should be canceled
     */
    boolean onDragStart(ModularGuiContext context, int button);

    @Override
    default IDraggable createDraggable(ModularGuiContext ctx, int button) {
        return this;
    }

    /**
     * The dragging has ended. If the dragging is successful or not and what happens next is up to the implementor.
     */
    void onDragEnd(ModularGuiContext context);

    /**
     * Called when dragging is canceled. This happens when the gui is closed while this is still being dragged.
     */
    default void onDragCancel(ModularGuiContext context) {}

    void onDrag(ModularGuiContext context, int mouseButton, double timeSinceLastClick);

    /**
     * @return the size and pos during move
     */
    @Nullable
    Area getMovingArea();

    boolean isMoving();

    void setMoving(boolean moving);

    void transform(IViewportStack viewportStack);
}
