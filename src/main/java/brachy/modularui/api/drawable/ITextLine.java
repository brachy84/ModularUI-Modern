package brachy.modularui.api.drawable;

import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;

import org.jspecify.annotations.Nullable;

public interface ITextLine {

    int getWidth();

    int getHeight(Font font);

    void draw(GuiContext context, Font font, float x, float y, int color, boolean shadow, int availableWidth, int availableHeight);

    @Nullable Object getHoveringElement(Font font, int x, int y);
}
