package brachy.modularui.widget;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.layout.IViewport;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.math.MathUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true, chain = true)
public class SplitView<W extends SplitView<W>> extends AbstractParentWidget<IWidget, W> implements Interactable, IViewport {

    private static final int HANDLE_SIZE = 4;
    private static final int HANDLE_SIZE_OFFSET = -HANDLE_SIZE / 2;

    @Getter
    private IWidget startWidget, endWidget;
    @Getter
    @Setter
    private GuiAxis splitAxis;
    private float splitValue = 0.5f;
    private float min = 0.05f, max = 1 - 0.05f;
    private boolean isDragging = false;
    @Getter
    @Setter
    private IDrawable handleTexture = new Rectangle().color(Color.withAlpha(0xFF404040, 0.2f));

    public SplitView(GuiAxis axis) {
        this.splitAxis = axis;
    }

    @Override
    public void afterInit() {
        resize(this.splitValue);
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            var widgetThemeEntry = getWidgetTheme(getPanel().getTheme());
            var widgetTheme = getActiveWidgetTheme(widgetThemeEntry, isHovering());
            var a = this.startWidget.getArea();
            if (this.splitAxis.isHorizontal()) {
                int start = a.ry + a.height + getArea().getPadding().top() + a.getMargin().top();
                this.handleTexture.draw(context, getArea().getPadding().left(), start, getArea().paddedWidth(), HANDLE_SIZE, widgetTheme);
            } else {
                int start = a.ry + a.width + getArea().getPadding().left() + a.getMargin().left();
                this.handleTexture.draw(context, start, getArea().getPadding().top(), HANDLE_SIZE, getArea().paddedHeight(), widgetTheme);
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        int p = getContext().getMouse(this.splitAxis.getOther());
        int pixelSplitValue = getSplitValueInPixel();
        if (p >= pixelSplitValue + HANDLE_SIZE_OFFSET && p < pixelSplitValue - HANDLE_SIZE_OFFSET) {
            this.isDragging = true;
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseReleased(int button) {
        boolean wasDragging = this.isDragging;
        this.isDragging = false;
        return wasDragging;
    }

    @Override
    public void onMouseDrag(int button, double dragX, double dragY) {
        resize(posToValue(getContext().getMouse(this.splitAxis.getOther())));
    }

    public float posToValue(int p) {
        return (p + HANDLE_SIZE_OFFSET) / (float) (getArea().getSize(this.splitAxis.getOther()) - HANDLE_SIZE);
    }

    public int getSplitValueInPixel() {
        return (int) (this.splitValue * getArea().getSize(this.splitAxis.getOther()));
    }

    private void resize(float splitValue) {
        float old = this.splitValue;
        this.splitValue = MathUtils.clamp(splitValue, this.min, this.max);
        if (this.splitAxis.isHorizontal()) {
            this.startWidget.resizer().topRel(0).leftRel(0).widthRel(1f).heightRelOffset(this.splitValue, HANDLE_SIZE_OFFSET);
            this.endWidget.resizer().bottom(0).leftRel(0).widthRel(1f).heightRelOffset(1 - this.splitValue, HANDLE_SIZE_OFFSET);

        } else {
            this.startWidget.resizer().leftRel(0).topRel(0).heightRel(1f).widthRelOffset(this.splitValue, HANDLE_SIZE_OFFSET);
            this.endWidget.resizer().rightRel(0).topRel(0).heightRel(1f).widthRelOffset(1 - this.splitValue, HANDLE_SIZE_OFFSET);
        }
        if (isValid() && this.splitValue != old) {
            if (this.startWidget instanceof SplitView<?> subSplit && subSplit.splitAxis == this.splitAxis) {
                float currentSubValue = old * subSplit.splitValue;
                subSplit.resize(currentSubValue / this.splitValue);
            }
            if (this.endWidget instanceof SplitView<?> subSplit && subSplit.splitAxis == this.splitAxis) {
                float currentSubValue = old + (1 - old) * subSplit.splitValue;
                subSplit.resize((currentSubValue - this.splitValue) / (1 - this.splitValue));
            }
        }
        scheduleResize();
    }

    @Override
    protected IWidget castToType(IWidget widget) {
        return widget;
    }

    public W splitValue(float splitValue) {
        if (isValid()) resize(splitValue);
        else this.splitValue = MathUtils.clamp(splitValue, this.min, this.max);
        return getThis();
    }

    public W bounds(float min, float max) {
        this.min = MathUtils.clamp(min, 0.001f, 0.999f);
        this.max = MathUtils.clamp(max, 0.001f, 0.999f);
        return getThis();
    }

    public W startWidget(IWidget widget) {
        if (this.startWidget != null) remove(this.startWidget);
        this.startWidget = widget;
        addChild(this.startWidget, -1);
        return getThis();
    }

    public W endWidget(IWidget widget) {
        if (this.endWidget != null) remove(this.endWidget);
        this.endWidget = widget;
        addChild(this.endWidget, -1);
        return getThis();
    }
}
