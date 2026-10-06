package brachy.modularui.integration.legendarytooltips;

import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.drawable.TextLineTooltipComponent;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.utils.TooltipLines;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import com.mojang.datafixers.util.Either;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

import com.anthonyhilyard.iceberg.util.Tooltips;
import com.anthonyhilyard.legendarytooltips.config.LegendaryTooltipsConfig;
import com.anthonyhilyard.legendarytooltips.tooltip.ItemModelComponent;
import com.anthonyhilyard.legendarytooltips.tooltip.TooltipDecor;
import org.jetbrains.annotations.Nullable;

/**
 * Legendary Tooltips does part of its tooltip layout while rendering vanilla tooltips, which doesn't happen for rich
 * tooltips. This replicates the item model next to the centered title and the separator below the title.
 */
public final class LegendaryTooltipsCompat {

    private static final int MIN_WIDTH = 48;

    private static int borderColor;

    static {
        // Legendary Tooltips sets the frame colors in the color event, which rich tooltips post as well
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, RenderTooltipEvent.Color.class,
                event -> borderColor = event.getBorderStart());
    }

    private LegendaryTooltipsCompat() {}

    /**
     * Applies the layout to tooltip lines after Legendary Tooltips modified them in
     * {@link RenderTooltipEvent.GatherComponents}.
     */
    public static void applyLayout(TooltipLines lines, ItemStack stack) {
        if (stack.isEmpty()) return;
        int titleIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).left().isPresent()) {
                titleIndex = i;
                break;
            }
        }
        if (titleIndex < 0) return;

        ItemModelComponent model = null;
        if (titleIndex > 0 && lines.get(titleIndex - 1).right().orElse(null) instanceof ItemModelComponent component) {
            model = component;
        }
        boolean centered = LegendaryTooltipsConfig.INSTANCE.centeredTitle.get();
        if (model != null || centered) {
            Component title = lines.get(titleIndex).left().orElseThrow();
            if (model != null) {
                // the title is already padded to make room for the model, the empty line after it is replaced by the
                // height of the title line
                if (titleIndex + 1 < lines.size() && isBlank(lines.get(titleIndex + 1))) {
                    lines.remove(titleIndex + 1);
                }
                lines.remove(titleIndex - 1);
                titleIndex--;
            }
            lines.set(titleIndex, textLine(new TitleLine(title, model, centered)));
        }

        for (int i = titleIndex + 1; i < lines.size(); i++) {
            if (lines.get(i).right().orElse(null) instanceof Tooltips.TitleBreakComponent) {
                if (LegendaryTooltipsConfig.INSTANCE.nameSeparator.get() &&
                        LegendaryTooltipsConfig.INSTANCE.getFrameDefinition(stack).index() != -2) {
                    lines.set(i, textLine(new SeparatorLine()));
                } else {
                    lines.remove(i);
                }
                break;
            }
        }
    }

    private static boolean isBlank(Either<Component, TooltipComponent> line) {
        return line.left().map(text -> text.getString().isBlank()).orElse(false);
    }

    private static Either<Component, TooltipComponent> textLine(ITextLine line) {
        return Either.right(new TextLineTooltipComponent(line));
    }

    private static Font getFont() {
        return Minecraft.getInstance().font;
    }

    private static class TitleLine implements ITextLine {

        private static final int MODEL_HEIGHT = 22;

        private final FormattedCharSequence title;
        @Nullable
        private final ClientTooltipComponent model;
        private final boolean centered;
        private final int titleWidth;

        private TitleLine(Component title, @Nullable ClientTooltipComponent model, boolean centered) {
            this.title = title.getVisualOrderText();
            this.model = model;
            this.centered = centered;
            this.titleWidth = getFont().width(this.title);
        }

        @Override
        public int getWidth() {
            if (LegendaryTooltipsConfig.INSTANCE.enforceMinimumWidth.get()) {
                return Math.max(this.titleWidth, MIN_WIDTH);
            }
            return this.titleWidth;
        }

        @Override
        public int getHeight(Font font) {
            return this.model != null ? MODEL_HEIGHT : font.lineHeight + 1;
        }

        @Override
        public void draw(GuiContext context, Font font, float x, float y, int color, boolean shadow,
                         int availableWidth, int availableHeight) {
            float textY = y;
            if (this.model != null) {
                this.model.renderImage(font, (int) x, (int) y, context.getGraphics());
                textY += (MODEL_HEIGHT - font.lineHeight) / 2;
            }
            float textX = this.centered ? x + (availableWidth - this.titleWidth) / 2 : x;
            context.getGraphics().drawString(font, this.title, textX, textY, color, shadow);
        }

        @Override
        public Object getHoveringElement(Font font, int x, int y) {
            return null;
        }
    }

    private static class SeparatorLine implements ITextLine {

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight(Font font) {
            return 3;
        }

        @Override
        public void draw(GuiContext context, Font font, float x, float y, int color, boolean shadow,
                         int availableWidth, int availableHeight) {
            TooltipDecor.drawSeparator(context.getGraphics().pose(), (int) x - 2, (int) y + 1, availableWidth,
                    borderColor);
        }

        @Override
        public Object getHoveringElement(Font font, int x, int y) {
            return null;
        }
    }
}
