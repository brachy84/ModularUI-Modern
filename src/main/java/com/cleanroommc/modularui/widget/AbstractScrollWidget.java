package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A scrollable parent widget. Children can be added.
 *
 * @param <I> type of children (in most cases just {@link IWidget})
 * @param <W> type of this widget
 */
public abstract class AbstractScrollWidget<I extends IWidget, W extends AbstractScrollWidget<I, W>>
        extends AbstractParentWidget<I, W> implements IViewport, Interactable {

    private final ScrollArea scroll = new ScrollArea();
    private boolean scrollXActive, scrollYActive;

    public AbstractScrollWidget(@Nullable HorizontalScrollData x, @Nullable VerticalScrollData y) {
        super();
        this.scroll.setScrollX(x);
        this.scroll.setScrollY(y);
        listenGuiAction((IGuiAction.MouseReleased) (mouseX, mouseY, button) -> {
            this.scroll.mouseReleased(getContext());
            return false;
        });
    }

    @Override
    public Area getArea() {
        return this.scroll;
    }

    public ScrollArea getScrollArea() {
        return this.scroll;
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        stack.translate(-getScrollX(), -getScrollY());
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        // if 'widgets.peek() == this' is true, only then this widget is hovered
        // we should require this since a stencil is applied to this widget
        if (widgets.peek() == this && !getScrollArea().isInsideScrollbarArea(x, y)) {
            IViewport.super.getWidgetsAt(stack, widgets, x, y);
        }
    }

    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        this.scroll.applyWidgetTheme(getPanel().getTheme().getScrollbarTheme().getTheme(isHovering()));
        if (onOpen) checkScrollbarActive(true);
        getScrollArea().getScrollPadding().scrollPaddingAll(0);
        applyAdditionalOffset(this.scroll.getScrollX());
        applyAdditionalOffset(this.scroll.getScrollY());
    }

    private void checkScrollbarActive(boolean onOpen) {
        boolean scrollYActive = this.scroll.getScrollY() != null &&
                this.scroll.getScrollY().isScrollBarActive(getScrollArea());
        boolean scrollXActive = this.scroll.getScrollX() != null &&
                this.scroll.getScrollX().isScrollBarActive(getScrollArea(), this.scrollYActive);
        if (!onOpen && (scrollYActive != this.scrollYActive || scrollXActive != this.scrollXActive)) {
            scheduleResize();
        }
        this.scrollXActive = scrollXActive;
        this.scrollYActive = scrollYActive;
    }

    private void applyAdditionalOffset(ScrollData data) {
        if (data != null && data.isScrollBarActive(getScrollArea())) {
            getScrollArea().getScrollPadding().scrollPadding(data.getAxis().getOther(), data.isAxisStart(),
                    data.getThickness());
        }
    }

    @Override
    public boolean canHover() {
        return super.canHover() ||
                this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        ModularGuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        return this.scroll.mouseScroll(getContext());
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        this.scroll.mouseReleased(getContext());
        return false;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        checkScrollbarActive(false);
        this.scroll.drag(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            context.getStencil().pushAtZero(this.scroll);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            context.getStencil().pop();
            WidgetThemeEntry<WidgetTheme> scrollbarTheme = getPanel().getTheme().getScrollbarTheme();
            this.scroll.drawScrollbar(context, scrollbarTheme.getTheme(isHovering()),
                    scrollbarTheme.theme().getBackground());
        }
    }

    public int getScrollX() {
        return this.scroll.getScrollX() != null ? this.scroll.getScrollX().getScroll() : 0;
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }
}
