package brachy.modularui.widgets;

import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.layout.ILayoutWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.AbstractParentWidget;

import com.mojang.blaze3d.platform.InputConstants;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class CollapsableList extends AbstractParentWidget<IWidget, CollapsableList> implements Interactable, ILayoutWidget {

    @Getter
    private boolean expanded = true;
    @Getter
    private IIcon collapsedIcon = GuiTextures.MOVE_RIGHT.asIcon().size(5, 10);
    private IIcon expandedIcon = GuiTextures.MOVE_DOWN.asIcon().size(10, 5);
    private IWidget title;
    @Getter private boolean collapseDisabledChildren = true;
    @Getter private int childPadding = 2;
    @Getter private int indent = 4;

    public CollapsableList() {
        coverChildrenHeight().fullWidth();
    }

    protected IIcon getCurrentIcon() {
        return this.expanded ? this.expandedIcon : this.collapsedIcon;
    }

    @Override
    public void onInit() {
        for (IWidget widget : getTypeChildren()) {
            if (widget != this.title) {
                widget.setEnabled(expanded);
            }
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        var iw = Math.max(this.collapsedIcon.getWidth() + this.collapsedIcon.getMargin().horizontal(), this.expandedIcon.getWidth() + this.expandedIcon.getMargin().horizontal());
        var ih = Math.max(this.collapsedIcon.getHeight() + this.collapsedIcon.getMargin().vertical(), this.expandedIcon.getHeight() + this.expandedIcon.getMargin().vertical());
        int space = 0;
        if (this.title != null) space = this.title.getArea().requestedHeight();
        getCurrentIcon().draw(context, getArea().getPadding().left(), getArea().getPadding().top(), iw, Math.max(space, ih), getActiveWidgetTheme(widgetTheme, isHovering()));
    }

    @Override
    public boolean layoutWidgets() {
        int p = getArea().getPadding().top();
        var iw = Math.max(this.collapsedIcon.getWidth() + this.collapsedIcon.getMargin().horizontal(), this.expandedIcon.getWidth() + this.expandedIcon.getMargin().horizontal());
        var ih = Math.max(this.collapsedIcon.getHeight() + this.collapsedIcon.getMargin().vertical(), this.expandedIcon.getHeight() + this.expandedIcon.getMargin().vertical());
        if (this.title != null) {
            int s = Math.max(ih, this.title.getArea().requestedHeight());
            this.title.getArea().ry = p + s / 2 - this.title.getArea().requestedHeight() / 2;
            this.title.getArea().rx = getArea().getPadding().left() + iw;
            this.title.getArea().getMargin().left(iw);
            this.title.resizer().setPosResized(true, true);
            this.title.resizer().setMarginPaddingApplied(true);
            p += s + this.childPadding;
        }
        int ep = 0;
        for (IWidget widget : getTypeChildren()) {
            if (widget == this.title) continue;
            if (shouldIgnoreChildSize(widget)) {
                widget.resizer().updateResized();
                widget.resizer().setMarginPaddingApplied(true);
                continue;
            }
            if (!widget.resizer().isHeightCalculated()) return false;
            ep += widget.getArea().getMargin().top();
            widget.getArea().ry = p + ep;
            ep += widget.getArea().height + widget.getArea().getMargin().bottom() + this.childPadding;
            widget.resizer().setYResized(true);
            widget.resizer().setYMarginPaddingApplied(true);
        }
        if (this.expanded) p += ep;
        p += getArea().getPadding().bottom();
        //resizer().setMarginPaddingApplied(true);
        return true;
    }

    @Override
    public boolean postLayoutWidgets() {
        var iw = Math.max(this.collapsedIcon.getWidth() + this.collapsedIcon.getMargin().horizontal(), this.expandedIcon.getWidth() + this.expandedIcon.getMargin().horizontal());
        int l = getArea().getPadding().left();
        for (IWidget widget : getTypeChildren()) {
            if (widget == this.title) continue;
            if (shouldIgnoreChildSize(widget)) {
                widget.resizer().updateResized();
                widget.resizer().setXMarginPaddingApplied(true);
                continue;
            }
            widget.getArea().rx = l + iw + this.indent;
            widget.getArea().getMargin().left(iw + this.indent);
            widget.resizer().setXResized(true);
            widget.resizer().setXMarginPaddingApplied(true);
        }
        return true;
    }

    @Override
    public boolean shouldIgnoreChildSize(IWidget child) {
        return this.collapseDisabledChildren && !child.isEnabled();
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            expanded(!this.expanded);
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    public CollapsableList expanded(boolean expanded) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            if (isValid()) {
                for (IWidget widget : getTypeChildren()) {
                    if (widget != this.title) {
                        widget.setEnabled(expanded);
                    }
                }
                scheduleResize();
            }
        }
        return this;
    }

    @Override
    protected IWidget castToType(IWidget widget) {
        return widget;
    }

    public CollapsableList title(IWidget widget) {
        if (this.title != null) remove(0);
        addChild(widget, 0);
        this.title = widget;
        return this;
    }

    public CollapsableList child(IWidget list) {
        addChild(list, -1);
        return this;
    }

    public CollapsableList collapsedIcon(IIcon icon) {
        this.collapsedIcon = icon != null ? icon : IIcon.EMPTY;
        return this;
    }

    public CollapsableList expandedIcon(IIcon icon) {
        this.expandedIcon = icon != null ? icon : IIcon.EMPTY;
        return this;
    }

    /**
     * Sets if disabled children should be collapsed. This means that if a child changes enabled state, this widget gets notified and
     * re-layouts its children. Children which are disabled will not be considered during layout, so that the list will not appear to have
     * empty spots. This is enabled by default on lists.
     *
     * @param doCollapse true if disabled children should be collapsed.
     * @return this
     */
    public CollapsableList collapseDisabledChildren(boolean doCollapse) {
        this.collapseDisabledChildren = doCollapse;
        return this;
    }

    public CollapsableList childPadding(int childPadding) {
        this.childPadding = childPadding;
        return this;
    }

    public CollapsableList indent(int indent) {
        this.indent = indent;
        return this;
    }
}
