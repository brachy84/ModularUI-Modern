package brachy.modularui.widget;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IDelegatingWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.MutableSingletonList;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widget.sizer.StandardResizer;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DelegatingWidget extends AbstractWidget implements IDelegatingWidget {

    private final MutableSingletonList<IWidget> delegate = new MutableSingletonList<>();

    public DelegatingWidget(IWidget delegate) {
        this.delegate.set(delegate);
        resizer(new StandardResizer(this));
    }

    protected void setDelegate(IWidget delegate) {
        if (this.delegate.hasValue()) {
            if (this.delegate.hasNonNullValue()) {
                this.delegate.get().dispose();
            }
            this.delegate.remove();
        }
        if (delegate != null) {
            this.delegate.set(delegate);
            if (isValid()) {
                initialise(getParent(), true);
                delegate.scheduleResize();
            }
            onChangeDelegate(delegate);
        }
    }

    protected void onChangeDelegate(IWidget delegate) {}

    @Override
    public @NotNull List<IWidget> getChildren() {
        return getDelegate() != null ? getDelegate().getChildren() : Collections.emptyList();
    }

    @Override
    public void onInit() {
        super.onInit();
        this.delegate.forEach(w -> {
            w.initialise(this, false);
        });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.delegate.apply(IWidget::onUpdate);
        if (getDelegate() != null) getDelegate().onUpdate();
    }

    @Override
    public void onMouseStartHover() {
        super.onMouseStartHover();
        this.delegate.apply(IWidget::onMouseStartHover);
    }

    @Override
    public void onMouseEndHover() {
        super.onMouseEndHover();
        this.delegate.apply(IWidget::onMouseEndHover);
    }

    @Override
    public void onMouseEnterArea() {
        super.onMouseEnterArea();
        this.delegate.apply(IWidget::onMouseEnterArea);
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        this.delegate.apply(IWidget::onMouseLeaveArea);
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        this.delegate.apply(w -> w.drawBackground(context, widgetTheme));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        this.delegate.apply(w -> w.draw(context, widgetTheme));
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        this.delegate.apply(w -> w.drawOverlay(context, widgetTheme));
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        this.delegate.apply(w -> w.drawForeground(context));
    }

    @Override
    public @NotNull StandardResizer resizer() {
        return this.delegate.toValue(IWidget::resizer, super.resizer());
    }

    @Override
    public Area getArea() {
        return this.delegate.toValue(IWidget::getArea, super.getArea());
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && this.delegate.toBool(IWidget::isEnabled, true);
    }

    @Override
    public void transform(IViewportStack stack) {
        this.delegate.apply(w -> w.transform(stack));
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return this.delegate.toBool(w -> w.canBeSeen(stack), false);
    }

    @Override
    public boolean canHover() {
        return this.delegate.toBool(IWidget::canHover, false);
    }

    @Override
    public boolean canClickThrough() {
        return this.delegate.toBool(IWidget::canClickThrough, false);
    }

    @Override
    public boolean canHoverThrough() {
        return this.delegate.toBool(IWidget::canHoverThrough, false);
    }

    @Override
    public int getDefaultWidth() {
        return this.delegate.toInt(IWidget::getDefaultWidth, super.getDefaultWidth());
    }

    @Override
    public int getDefaultHeight() {
        return this.delegate.toInt(IWidget::getDefaultHeight, super.getDefaultWidth());
    }

    @Override
    public IWidget getDelegate() {
        return delegate.getOrNull();
    }

    @Override
    public IWidget copy() {
        return new DelegatingWidget(this.delegate.hasNonNullValue() ? this.delegate.get().copy() : null);
    }
}
