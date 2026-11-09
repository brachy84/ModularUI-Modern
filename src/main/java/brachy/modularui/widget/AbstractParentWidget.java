package brachy.modularui.widget;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widgets.VoidWidget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A widget which can hold any amount of children.
 *
 * @param <I> type of children (in most cases just {@link IWidget}). Use {@link VoidWidget} if no children should be
 *            added.
 * @param <W> type of this widget
 */
public abstract class AbstractParentWidget<I extends IWidget, W extends AbstractParentWidget<I, W>> extends Widget<W> {

    private final List<I> children = new ArrayList<>();

    /**
     * A list of all children of this widget. The list is modifiable contrary to the annotation.
     * This just means that you shouldn't carelessly modify the list. Adding to the list also requires initialising the
     * new child.
     * Removing requires disposing the old child.
     *
     * @return a view of all children.
     */
    @SuppressWarnings("unchecked")
    @UnmodifiableView
    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return (List<IWidget>) this.children;
    }

    /**
     * A list of all children of this widget with the given children type {@link I}. The list is modifiable contrary to
     * the annotation.
     * This just means that you shouldn't carelessly modify the list. Adding to the list also requires initialising the
     * new child.
     * Removing requires disposing the old child.
     *
     * @return a view of all children.
     */
    @UnmodifiableView
    public List<I> getTypeChildren() {
        return this.children;
    }

    @Override
    public void visitTransformChildren(UnaryOperator<IWidget> op) {
        visitTransformTypedChildren(child -> {
            IWidget w = op.apply(child);
            return canCastToType(w) ? castToType(w) : child;
        });
    }

    public void visitTransformTypedChildren(UnaryOperator<I> op) {
        List<I> children = getTypeChildren();
        for (int i = 0; i < children.size(); i++) {
            I current = children.get(i);
            I transformed = op.apply(children.get(i));
            replace(i, current, transformed);
        }
    }

    @Override
    public boolean canHover() {
        if (IDrawable.isVisible(getBackground()) ||
                IDrawable.isVisible(getHoverBackground()) ||
                IDrawable.isVisible(getHoverOverlay()) ||
                getTooltip() != null)
            return true;
        WidgetThemeEntry<?> widgetTheme = getWidgetTheme(getPanel().getTheme());
        if (getBackground() == null && IDrawable.isVisible(widgetTheme.theme().getBackground())) return true;
        return getHoverBackground() == null && IDrawable.isVisible(widgetTheme.hoverTheme().getBackground());
    }

    @Override
    public boolean canClickThrough() {
        return !canHover();
    }

    @Override
    public boolean canHoverThrough() {
        return !canHover();
    }

    protected boolean addChildRaw(IWidget child, int index, boolean silent) {
        if (canCastToType(child)) return addChild(castToType(child), index);
        if (!silent) ModularUI.LOGGER.error("Can't add child of type {} to {}", child.getClass().getSimpleName(), this);
        return false;
    }

    private int wrapIndex(int i) {
        if (i < 0) i += getChildren().size();
        return i;
    }

    protected boolean addChild(I child, int index) {
        if (child == null || child == this || getChildren().contains(child)) {
            return false;
        }
        if (child instanceof ModularPanel) {
            throw new IllegalArgumentException(
                    "ModularPanel<?> should not be added as child widget; Use ModularScreen#openPanel instead");
        }
        if (!isChildValid(child)) {
            throw new IllegalArgumentException("Child '" + child + "' is not valid for parent '" + this + "'!");
        }
        if (index < 0) index += getChildren().size() + 1;
        this.children.add(index, child);
        if (isValid()) {
            child.initialise(this, true);
        }
        onChildAdd(child);
        return true;
    }

    protected boolean removeRaw(IWidget child) {
        return canCastToType(child) && remove(castToType(child));
    }

    protected boolean remove(I child) {
        if (this.children.remove(child)) {
            if (isValid()) {
                child.dispose();
            }
            onChildRemove(child);
            return true;
        }
        return false;
    }

    protected boolean remove(int index) {
        index = wrapIndex(index);
        I child = this.children.remove(index);
        if (isValid()) {
            child.dispose();
        }
        onChildRemove(child);
        return true;
    }

    protected boolean replaceRaw(IWidget target, IWidget replacement) {
        return canCastToType(replacement) && replace(target, castToType(replacement));
    }

    protected boolean replace(IWidget target, I replacement) {
        for (int i = 0; i < this.children.size(); i++) {
            I current = this.children.get(i);
            if (target == current) {
                return replace(i, current, replacement);
            }
        }
        return false;
    }

    protected boolean replace(int index, I replacement) {
        return replace(index, this.children.get(wrapIndex(index)), replacement);
    }

    private boolean replace(int index, I current, I replacement) {
        if (current == replacement) return true;
        if (!isChildValid(replacement)) return false;
        remove(index);
        addChild(replacement, index);
        return true;
    }

    protected boolean removeAll() {
        if (this.children.isEmpty()) return true;
        for (I i : this.children) {
            if (isValid()) i.dispose();
            onChildRemove(i);
        }
        this.children.clear();
        return true;
    }

    protected boolean isChildValidRaw(IWidget child) {
        return canCastToType(child) && isChildValid(castToType(child));
    }

    protected boolean isChildValid(I child) {
        return true;
    }

    protected final boolean canCastToType(IWidget widget) {
        return widget == null || castToType(widget) != null;
    }

    protected abstract I castToType(IWidget widget);

    protected void onChildAdd(I child) {}

    protected void onChildRemove(I child) {}

    @Override
    public boolean applyModification(WidgetModification modification, IWidget childTarget) {
        if (modification.isAdd()) {
            addChildRaw(modification.widget().copy(), modification.index(), false);
            return true;
        }
        if (modification.isRemove()) {
            removeRaw(childTarget);
            return true;
        }
        if (modification.isReplace()) {
            replaceRaw(childTarget, modification.widget().copy());
            return true;
        }
        return false;
    }
}
