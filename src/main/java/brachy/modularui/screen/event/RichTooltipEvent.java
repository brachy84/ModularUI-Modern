package brachy.modularui.screen.event;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RichTooltipEvent {

    private RichTooltipEvent() {}

    /**
     * A rich tooltip event that is mean ONLY for rich tooltips.
     * {@link Gather.Pre} is invoked before the vanilla event.
     * {@link Gather.Post} is invoked after the vanilla event.
     * Both can be canceled and edited.
     */
    @Cancelable
    public static class Gather extends Event {

        @Getter private final IRichTextBuilder<?> tooltip;
        @Getter protected final ItemStack itemStack;
        @Getter protected final GuiContext guiContext;
        @Getter protected int x;
        @Getter protected int y;
        @Getter protected int screenWidth;
        @Getter protected int screenHeight;
        @Getter
        @Setter
        protected int maxWidth;

        protected Gather(IRichTextBuilder<?> tooltip, ItemStack stack, GuiContext guiContext,
                         int x, int y, int screenWidth, int screenHeight, int maxWidth) {
            this.tooltip = tooltip;
            this.itemStack = stack;
            this.guiContext = guiContext;
            this.x = x;
            this.y = y;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.maxWidth = maxWidth;
        }

        @Cancelable
        public static class Pre extends Gather {

            public Pre(IRichTextBuilder<?> tooltip, ItemStack stack, GuiContext guiContext,
                       int x, int y, int screenWidth, int screenHeight, int maxWidth) {
                super(tooltip, stack, guiContext, x, y, screenWidth, screenHeight, maxWidth);
            }
        }

        @Cancelable
        public static class Post extends Gather {

            public Post(IRichTextBuilder<?> tooltip, ItemStack stack, GuiContext guiContext,
                        int x, int y, int screenWidth, int screenHeight, int maxWidth) {
                super(tooltip, stack, guiContext, x, y, screenWidth, screenHeight, maxWidth);
            }
        }
    }

    @Cancelable
    public static class Pre extends RenderTooltipEvent.Pre {

        @Getter
        private final IRichTextBuilder<?> tooltip;

        public Pre(@NonNull ItemStack stack, @NonNull GuiGraphics graphics,
                   int x, int y, int screenWidth, int screenHeight, @NonNull Font font,
                   @NonNull List<ClientTooltipComponent> components, @NonNull ClientTooltipPositioner positioner,
                   IRichTextBuilder<?> tooltip) {
            super(stack, graphics, x, y, screenWidth, screenHeight, font, components, positioner);
            this.tooltip = tooltip;
        }
    }

    public static class Color extends RenderTooltipEvent.Color {

        @Getter
        private final IRichTextBuilder<?> tooltip;

        public Color(@NonNull ItemStack stack, @NonNull GuiGraphics graphics,
                     int x, int y, @NonNull Font font, int background, int borderStart, int borderEnd,
                     @NonNull List<ClientTooltipComponent> components, IRichTextBuilder<?> tooltip) {
            super(stack, graphics, x, y, font, background, borderStart, borderEnd, components);
            this.tooltip = tooltip;
        }
    }
}
