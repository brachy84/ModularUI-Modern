package brachy.modularui.integration.rei.recipe;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.util.RecipeDebugDecoratorUtil;
import brachy.modularui.integration.rei.ReiRecipeViewerSlot;
import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.RichTooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class ModularUIReiDisplay implements Display {

    private static final String SCREEN_NAME_PREFIX = "rei_display_";

    private final ResourceLocation recipeId;
    private final Supplier<IWidget> recipeUI;
    @Getter protected final CategoryIdentifier<?> categoryIdentifier;

    private boolean sizeCalculated = false;
    private int displayWidth, displayHeight;

    public ModularUIReiDisplay(ResourceLocation recipeId, Supplier<IWidget> recipeUI, CategoryIdentifier<?> categoryId) {
        this.recipeId = recipeId;
        this.recipeUI = recipeUI;
        this.categoryIdentifier = categoryId;
    }

    /**
     * Calculates the size of the recipe if not already done.<br>
     * This should be called in subclasses' constructors. Otherwise, the size of ALL the recipes in the same category are calculated
     * at once, which can make the game lag for a few seconds.
     */
    protected void calculateSize() {
        if (this.sizeCalculated) return;
        this.sizeCalculated = true;

        IWidget ui = this.recipeUI.get();
        int w = ui.resizer().getFixedPixelWidth(), h = ui.resizer().getFixedPixelHeight();
        if (w < 0 || h < 0) {
            ModularScreen screen = createScreen(ui, this.recipeId.getNamespace(), SCREEN_NAME_PREFIX + this.recipeId.getPath());
            w = EmbedHandler.getEmbedWidth(screen);
            h = EmbedHandler.getEmbedHeight(screen);
        }
        this.displayWidth = w;
        this.displayHeight = h;
    }

    public int getDisplayWidth() {
        calculateSize();
        return this.displayWidth;
    }

    public int getDisplayHeight() {
        calculateSize();
        return this.displayHeight;
    }

    private ModularScreen createScreen() {
        return createScreen(this.recipeUI.get(), this.recipeId.getNamespace(), SCREEN_NAME_PREFIX + this.recipeId.getPath());
    }

    @ApiStatus.OverrideOnly
    public ModularScreen createScreen(IWidget recipeUI, String owner, String name) {
        ModularPanel<?> panel;
        if (recipeUI instanceof ModularPanel<?> panel1) {
            panel = panel1;
        } else {
            panel = new ModularPanel<>(name);
            panel.coverChildren(60, 40)
                    .invisible()
                    .child(recipeUI);
        }
        if (!getInputEntries().isEmpty() && !getOutputEntries().isEmpty()) {
            panel = transform(panel);
        }
        ModularScreen screen = ModularScreen.createEmbed(owner, panel);
        screen.getContext().getUISettings().drawTooltipExternally(true);

        RecipeDebugDecoratorUtil.addRecipeDebugOverlays(screen);
        return screen;
    }

    public ModularPanel<?> transform(ModularPanel<?> panel) {
        Iterator<EntryIngredient> in = getInputEntries().iterator();
        Iterator<EntryIngredient> out = getOutputEntries().iterator();
        panel.visitTransformAllChildren(widget -> transformWidget(widget, in, out));
        return panel;
    }

    @ApiStatus.OverrideOnly
    public IWidget transformWidget(IWidget widget, Iterator<EntryIngredient> in, Iterator<EntryIngredient> out) {
        if (!(widget instanceof ReiRecipeViewerSlot<?> recipeViewerSlot)) return widget;

        if (recipeViewerSlot.recipeSlotRole() == RecipeSlotRole.OUTPUT) {
            // recipeViewerSlot.getSlotWidget().recipeContext(this);
        }

        return recipeViewerSlot;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(this.recipeId);
    }

    public List<Widget> createWidgets(Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new UIWrapperWidget(this, bounds.x, bounds.y));
        return widgets;
    }

    public static class UIWrapperWidget extends Widget {

        private final ModularScreen screen;
        private final float offsetX, offsetY;

        public UIWrapperWidget(ModularUIReiDisplay display, float offsetX, float offsetY) {
            this.screen = display.createScreen();
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            EmbedHandler.drawEmbed(this.screen, graphics, mouseX, mouseY, partialTick, () -> {
                screen.getContext().pushMatrix();
                screen.getContext().translate(this.offsetX, this.offsetY);
            }, () -> {
                screen.getContext().popMatrix();
            });
            Tooltip tooltip = this.getTooltip(TooltipContext.ofMouse());
            if (tooltip != null) {
                tooltip.queue();
            }
        }

        @Override
        public @Nullable Tooltip getTooltip(TooltipContext context) {
            if (!this.screen.getContext().getUISettings().drawTooltipExternally()) {
                return super.getTooltip(context);
            }

            IWidget hovered = this.screen.getContext().getTopHovered();
            if (hovered instanceof ITooltip<?> tooltip && tooltip.getTooltip() != null) {
                RichTooltip richTooltip = tooltip.getTooltip();
                if (richTooltip.autoUpdate()) richTooltip.markDirty();
                richTooltip.isEmpty(); // causes the tooltip to rebuild if necessary

                IRichTextBuilder<?> richTextBuilder = richTooltip.getRichText();
                if (richTextBuilder instanceof RichText richText) {
                    // scuffed conversion, but it mostly works
                    Tooltip tooltipBuilder = Tooltip.create(context.getPoint());
                    for (var line : richText.getAsText()) {
                        line.ifLeft(tooltipBuilder::add).ifRight(tooltipBuilder::add);
                    }
                    return tooltipBuilder;
                }
            }
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            //this.screen.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.screen.mousePressed(button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.screen.mouseReleased(button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return this.screen.mouseDragged(button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
            return this.screen.mouseScrolled(scrollDelta);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.screen.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return this.screen.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return this.screen.charTyped(codePoint, modifiers);
        }
    }
}
