package brachy.modularui.drawable;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Takes supplier of {@link IDrawable} and draws conditional drawable.
 * Return value of the supplier should be deterministic per render frame,
 * in order to apply {@link ITheme} to correct object.
 */
public record DynamicDrawable(Supplier<@Nullable IDrawable> supplier) implements IDrawable {

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        IDrawable drawable = this.supplier.get();
        if (drawable != null) {
            drawable.draw(context, x, y, width, height, widgetTheme);
        }
    }

    @Override
    public boolean canApplyTheme() {
        IDrawable drawable = this.supplier.get();
        if (drawable != null) {
            return drawable.canApplyTheme();
        } else {
            return false;
        }
    }
}
