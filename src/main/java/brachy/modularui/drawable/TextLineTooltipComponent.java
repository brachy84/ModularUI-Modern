package brachy.modularui.drawable;

import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.utils.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.NotNull;

/**
 * Exposes a {@link ITextLine}, which is its own line in rich text, as a tooltip component for vanilla tooltip code.
 */
public record TextLineTooltipComponent(ITextLine line) implements ClientTooltipComponent, TooltipComponent {

    @Override
    public int getHeight() {
        return this.line.getHeight(Minecraft.getInstance().font);
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return this.line.getWidth();
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        GuiContext context = GuiContext.getDefault();
        GuiGraphics lastGraphics = context.getGraphics();

        context.setGraphics(guiGraphics);
        this.line.draw(context, font, x, y, Color.WHITE.main, true, getWidth(font), getHeight());
        context.setGraphics(lastGraphics);
    }
}
