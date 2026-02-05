package com.cleanroommc.modularui.component;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.NotNull;

public record DrawableTooltipComponent(IDrawable drawable) implements ClientTooltipComponent, TooltipComponent {

    @Override
    public int getHeight() {
        if (drawable instanceof IIcon icon) {
            return icon.getHeight();
        } else if (drawable instanceof IKey key) {
            return key.asTextIcon().getHeight();
        } else {
            return 18;
        }
    }

    @Override
    public int getWidth(@NotNull Font font) {
        if (drawable instanceof IIcon icon) {
            return icon.getWidth();
        } else if (drawable instanceof IKey key) {
            return key.asTextIcon().getWidth();
        } else {
            return 18;
        }
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        GuiContext context = GuiContext.getDefault();
        GuiGraphics lastGraphics = context.getGraphics();

        context.setGraphics(guiGraphics);
        drawable.draw(context, x, y, getWidth(font), getHeight(), WidgetTheme.getDefault().theme());
        context.setGraphics(lastGraphics);
    }
}
