package brachy.modularui.integration.emi.recipe;

import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.emi.EmiRecipeViewerSlot;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.util.RecipeDebugDecoratorUtil;
import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.RichTooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class ModularUIEmiRecipe implements EmiRecipe {

    private static final String SCREEN_NAME_PREFIX = "emi_recipe_";
    private static final LoadingCache<ModularUIEmiRecipe, ModularScreen> PREVIEW_SCREEN_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(60))
            .initialCapacity(8)
            .maximumSize(32)
            .softValues()
            .build(new CacheLoader<>() {
                @Override
                public ModularScreen load(ModularUIEmiRecipe key) {
                    return key.createScreen();
                }

                @Override
                public ListenableFuture<ModularScreen> reload(ModularUIEmiRecipe key, ModularScreen oldValue) {
                    // if an old value is (somehow) available, reuse it
                    return Futures.immediateFuture(oldValue);
                }
            });

    private static ModularScreen getCachedModularScreen(ModularUIEmiRecipe recipe) {
        return PREVIEW_SCREEN_CACHE.getUnchecked(recipe);
    }

    @Getter private final ResourceLocation id;
    private final Supplier<IWidget> recipeUI;

    private boolean sizeCalculated = false;
    private Bounds bounds;
    private int displayWidth, displayHeight;

    public ModularUIEmiRecipe(ResourceLocation recipeId, Supplier<IWidget> recipeUI) {
        this.id = recipeId;
        this.recipeUI = recipeUI;
    }

    public ModularUIEmiRecipe(ResourceLocation recipeId, int width, int height, Supplier<IWidget> recipeUI) {
        this.id = recipeId;
        this.recipeUI = recipeUI;
        this.displayWidth = width;
        this.displayHeight = height;
        this.bounds = new Bounds(0, 0, this.displayWidth, this.displayHeight);
        this.sizeCalculated = true;
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
            ModularScreen screen = createScreen(ui, this.id.getNamespace(), SCREEN_NAME_PREFIX + this.id.getPath());
            w = EmbedHandler.getEmbedWidth(screen);
            h = EmbedHandler.getEmbedHeight(screen);
        }
        this.displayWidth = w;
        this.displayHeight = h;
        this.bounds = new Bounds(0, 0, this.displayWidth, this.displayHeight);
    }

    public Bounds getBounds() {
        calculateSize();
        return this.bounds;
    }

    @Override
    public int getDisplayWidth() {
        calculateSize();
        return this.displayWidth;
    }

    @Override
    public int getDisplayHeight() {
        calculateSize();
        return this.displayHeight;
    }

    private ModularScreen createScreen() {
        return createScreen(this.recipeUI.get(), this.id.getNamespace(), SCREEN_NAME_PREFIX + this.id.getPath());
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
        panel = transform(panel);

        ModularScreen screen = ModularScreen.createEmbed(owner, panel);
        screen.getContext().getUISettings().drawTooltipExternally(true);

        RecipeDebugDecoratorUtil.addRecipeDebugOverlays(screen);
        return screen;
    }

    @ApiStatus.OverrideOnly
    public ModularPanel<?> transform(ModularPanel<?> panel) {
        panel.visitTransformAllChildren(this::transformWidget);
        return panel;
    }

    @ApiStatus.OverrideOnly
    public IWidget transformWidget(IWidget widget) {
        if (!(widget instanceof EmiRecipeViewerSlot<?> recipeViewerSlot)) return widget;

        if (recipeViewerSlot.recipeSlotRole() == RecipeSlotRole.OUTPUT) {
            recipeViewerSlot.getSlotWidget().recipeContext(this);
        }

        return recipeViewerSlot;
    }

    private boolean useScreenCacheForNextWidgetQuery = false;

    /**
     * This is used to make EMI's recipe tooltip components use the screen cache because those normally recreate the widgets every frame
     */
    @ApiStatus.Internal
    public final void useScreenCacheForNextWidgetQuery() {
        useScreenCacheForNextWidgetQuery = true;
    }

    @MustBeInvokedByOverriders
    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (this.supportsRecipeTree()) {
            // emi complains when it cant find an output slot
            widgets.add(new SlotWidget(EmiStack.EMPTY, -1000, -1000).drawBack(false).recipeContext(this));
        }
        widgets.add(new UIWrapperWidget(this, useScreenCacheForNextWidgetQuery));
        useScreenCacheForNextWidgetQuery = false;
    }

    public static class UIWrapperWidget extends Widget {

        private final ModularScreen screen;

        @Getter
        private final Bounds bounds;

        public UIWrapperWidget(ModularUIEmiRecipe recipe, boolean useScreenCache) {
            this.screen = useScreenCache ? getCachedModularScreen(recipe) : recipe.createScreen();

            if (recipe.sizeCalculated) {
                this.bounds = recipe.getBounds();
            } else {
                this.bounds = new Bounds(0, 0, EmbedHandler.getEmbedWidth(screen), EmbedHandler.getEmbedHeight(screen));
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            EmbedHandler.drawEmbed(this.screen, graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            if (!this.screen.getContext().getUISettings().drawTooltipExternally()) {
                return super.getTooltip(mouseX, mouseY);
            }

            IWidget hovered = this.screen.getContext().getTopHovered();
            if (hovered instanceof ITooltip<?> tooltip && tooltip.getTooltip() != null) {
                RichTooltip richTooltip = tooltip.getTooltip();
                if (richTooltip.autoUpdate()) richTooltip.markDirty();
                // causes the tooltip to rebuild if necessary
                if (richTooltip.isEmpty()) return List.of();

                if (richTooltip.getRichText() instanceof RichText richText) {
                    // scuffed conversion, but it mostly works
                    return richText.getAsText().toClientTooltipComponents();
                }
            }
            return List.of();
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            return this.screen.mousePressed(button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.screen.keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            return this.screen.mouseScrolled(delta);
        }

        public boolean mouseDragged(int button, double dragX, double dragY) {
            return this.screen.mouseDragged(button, dragX, dragY);
        }

        public boolean mouseReleased(int button) {
            return this.screen.mouseReleased(button);
        }
    }
}
