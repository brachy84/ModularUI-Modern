package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.Widget;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class FluidDrawable implements IDrawable {

    private FluidStack fluid = null;

    public FluidDrawable() {}

    /**
     * Takes a fluid stack, it can be null but will not draw anything
     *
     * @param fluid - fluid stack to draw
     */
    public FluidDrawable(@NotNull FluidStack fluid) {
        setFluid(fluid);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawFluidTexture(context.getGraphics(), fluid, x, y, width, height, context.getCurrentDrawingZ());
    }

    @Override
    public int getDefaultWidth() {
        return 16;
    }

    @Override
    public int getDefaultHeight() {
        return 16;
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    public FluidDrawable setFluid(FluidStack fluid) {
        this.fluid = fluid;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof FluidDrawable that)) return false;

        return fluid.isFluidEqual(that.fluid);
    }

    @Override
    public int hashCode() {
        return fluid.hashCode();
    }
}
