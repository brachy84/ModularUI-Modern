package com.cleanroommc.modularui.client.component;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Box;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public record TooltipComponentIcon(ClientTooltipComponent clientComponent) implements IIcon {

    public TooltipComponentIcon(TooltipComponent component) {
        this(ClientTooltipComponent.create(component));
    }

    @OnlyIn(Dist.CLIENT)
    public TooltipComponentIcon {
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
        float ratio = 1.0f;
        if (width < clientComponent.getWidth(font)) {
            ratio = (float) width / clientComponent.getWidth(font);
        }
        if (height < clientComponent.getHeight()) {
            ratio = Math.min(ratio, (float) height / clientComponent.getHeight());
        }
        if (ratio != 1.0f) {
            context.graphicsPose().scale(ratio, ratio, 1.0f);
        }
        clientComponent.renderImage(font, x, y, context.getGraphics());

        context.graphicsPose().popPose();
    }
}
