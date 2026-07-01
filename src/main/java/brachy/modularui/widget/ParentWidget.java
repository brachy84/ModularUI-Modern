package brachy.modularui.widget;

import brachy.modularui.api.widget.IParentWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import java.util.Collections;
import java.util.List;

/**
 * A widget which can hold any amount of children.
 *
 * @param <W> type of this widget
 */
public class ParentWidget<W extends ParentWidget<W>> extends AbstractParentWidget<IWidget, W> implements IParentWidget<IWidget, W> {

    public static final MutableObjectCodec<ParentWidget<?>> CODEC = MutableObjectCodec.<ParentWidget<?>>builder()
            .instance(ParentWidget::new)
            .addFieldsOf(Widget.CODEC, w -> w)
            .addOpt("children", ParentWidget::setChildren, ParentWidget::getChildren, IWidget.CODEC.listOf(), Collections.emptyList())
            .build();

    private void setChildren(List<IWidget> children) {
        removeAll();
        children.forEach(w -> addChildRaw(w, -1, false));
    }

    public boolean addChild(IWidget child, int index) {
        return super.addChild(child, index);
    }

    @Override
    public boolean remove(IWidget child) {
        return super.remove(child);
    }

    @Override
    public boolean remove(int index) {
        return super.remove(index);
    }

    @Override
    protected boolean removeAll() {
        return super.removeAll();
    }

    @Override
    public IWidget castToType(IWidget widget) {
        return widget;
    }

    @Override
    public WidgetType<?> getType() {
        return WidgetType.PARENT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public W copyExact() {
        return (W) CODEC.copy(this);
    }
}
