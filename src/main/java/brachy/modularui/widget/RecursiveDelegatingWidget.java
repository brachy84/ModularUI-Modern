package brachy.modularui.widget;

import brachy.modularui.api.widget.IWidget;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public abstract class RecursiveDelegatingWidget<R extends RecursiveDelegatingWidget<R>> extends DelegatingWidget {

    private final List<R> children = new ArrayList<>();
    private final List<R> childrenWrapper = new AbstractList<>() {
        @Override
        public R get(int index) {
            checkSize();
            var p = getDelegate();
            if (p == null || !p.hasChildren()) throw new IndexOutOfBoundsException(index);
            var real = p.getChildren().get(index);
            var del = children.get(index);
            if (real == null) {
                if (del != null) {
                    RecursiveDelegatingWidget.this.remove(del);
                    children.set(index, null);
                }
                return null;
            }
            if (del == null || del.getDelegate() != real) {
                RecursiveDelegatingWidget.this.remove(del);
                del = RecursiveDelegatingWidget.this.createChildDelegate(real);
                RecursiveDelegatingWidget.this.init(del);
                children.set(index, del);
            }
            return del;
        }

        @Override
        public int size() {
            return RecursiveDelegatingWidget.this.checkSize();
        }
    };
    private final List<IWidget> childrenRawTypeWrapper = new AbstractList<>() {

        @Override
        public IWidget get(int index) {
            return childrenWrapper.get(index);
        }

        @Override
        public int size() {
            return childrenWrapper.size();
        }
    };

    public RecursiveDelegatingWidget(IWidget delegate) {
        super(delegate);
    }

    private int checkSize() {
        var p = getDelegate();
        var realChildren = p.getChildren();
        while (realChildren.size() < children.size()) {
            remove(children.remove(children.size() - 1));
        }
        while (realChildren.size() > children.size()) {
            children.add(null);
        }
        return children.size();
    }

    private void remove(IWidget widget) {
        if (widget != null) {
            widget.dispose();
        }
    }

    private void init(IWidget widget) {
        if (isValid()) {
            widget.initialise(this, true);
            scheduleResize();
        }
    }

    protected abstract R createChildDelegate(IWidget widget);

    @Override
    public @NotNull List<IWidget> getChildren() {
        return childrenRawTypeWrapper;
    }

    public List<R> getTypedChildren() {
        return childrenWrapper;
    }
}
