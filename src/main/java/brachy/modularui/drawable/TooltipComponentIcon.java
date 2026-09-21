package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.sizer.Box;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public record TooltipComponentIcon(ClientTooltipComponent clientComponent, boolean forceFitInBounds) implements IIcon {

    public TooltipComponentIcon(ClientTooltipComponent clientComponent) {
        this(clientComponent, true);
    }

    public TooltipComponentIcon(TooltipComponent component, boolean forceFitInBounds) {
        this(ClientTooltipComponent.create(component), forceFitInBounds);
    }

    public TooltipComponentIcon(TooltipComponent component) {
        this(ClientTooltipComponent.create(component), true);
    }

    @Override
    public @Nullable IDrawable getWrappedDrawable() {
        return null;
    }

    @Override
    public int getWidth() {
        return clientComponent.getWidth(Minecraft.getInstance().font);
    }

    @Override
    public int getHeight() {
        return clientComponent.getHeight();
    }

    @Override
    public Box getMargin() {
        return Box.ZERO;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        Font font = context.getFont();
        context.graphicsPose().pushPose();

        // rescale the tooltip component if we happen to go out of its bounds
        if (forceFitInBounds) {
            float ratio = 1.0f;
            if (width < clientComponent.getWidth(font)) {
                ratio = (float) width / clientComponent.getWidth(font);
            }
            if (height < clientComponent.getHeight()) {
                ratio = Math.min(ratio, (float) height / clientComponent.getHeight());
            }
            if (!Mth.equal(ratio, 1.0f)) {
                context.graphicsPose().scale(ratio, ratio, 1.0f);
            }
        }
        clientComponent.renderText(font, x, y, context.getLastGraphicsPose(), context.getGraphics().bufferSource());
        clientComponent.renderImage(font, x, y, context.getGraphics());

        context.graphicsPose().popPose();
    }
}
