package brachy.modularui.integration.jei.recipe;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.core.mixins.jei.RecipeLayoutAccessor;
import brachy.modularui.core.mixins.jei.RecipeLayoutBuilderAccessor;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.jei.JeiRecipeViewerSlot;
import brachy.modularui.integration.jei.ModularUIJeiPlugin;
import brachy.modularui.integration.recipeviewer.RecipeViewerUtils;
import brachy.modularui.integration.recipeviewer.util.RecipeDebugDecoratorUtil;
import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.RichTooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@ApiStatus.Experimental
public abstract class ModularUIJeiCategory<T> implements IRecipeCategory<T> {

    public static final String SCREEN_NAME_PREFIX = "jei_recipe_";

    private final LoadingCache<T, Dimensions> displaySizeCache;

    private final Function<T, IWidget> recipeUI;
    private final Function<T, ResourceLocation> recipeIdGetter;

    protected ModularUIJeiCategory(Function<T, IWidget> recipeUI, Function<T, ResourceLocation> recipeIdGetter) {
        this.recipeUI = recipeUI;
        this.recipeIdGetter = recipeIdGetter;

        this.displaySizeCache = CacheBuilder.newBuilder()
                .initialCapacity(64)
                .build(new CacheLoader<>() {
                    @Override
                    public Dimensions load(T recipe) {
                        return ModularUIJeiCategory.this.calculateSize(recipe);
                    }
                });
    }

    /**
     * Calculates the size of the recipe.
     */
    @ApiStatus.OverrideOnly
    protected Dimensions calculateSize(T recipe) {
        IWidget ui = this.recipeUI.apply(recipe);
        ResourceLocation id = this.recipeIdGetter.apply(recipe);
        int w = ui.resizer().getFixedPixelWidth(), h = ui.resizer().getFixedPixelHeight();
        if (w < 0 || h < 0) {
            ModularScreen screen = createScreen(ui, id.getNamespace(), SCREEN_NAME_PREFIX + id.getPath(), recipe);
            w = EmbedHandler.getEmbedWidth(screen);
            h = EmbedHandler.getEmbedHeight(screen);
        }
        return new Dimensions(w, h);
    }

    /**
     * Calculates and caches the size of the recipe if not already done.<br>
     * This should be called in subclasses' {@link #setupRecipeIngredients(IRecipeLayoutBuilder, Object, IFocusGroup) setupRecipeIngredients} methods.
     * Otherwise, the size of ALL the recipes in the same category are calculated at once, which can make the game lag for a few seconds.
     */
    protected final void calculateAndCacheSize(T recipe) {
        this.displaySizeCache.getUnchecked(recipe);
    }

    /**
     * Return the maximum expected display width here.<br>
     * You should also return a per-category display width that's at most this value in {@link #getWidth(Object)} if the default value
     * doesn't suit your needs.
     *
     * @return The maximum expected display width
     */
    public abstract int getMaxWidth();

    /**
     * Return the maximum expected display height here.<br>
     * You should also return a per-category display height that's at most this value in {@link #getHeight(Object)} if the default value
     * doesn't suit your needs.
     *
     * @return The maximum expected display height
     */
    public abstract int getMaxHeight();

    /**
     * Sets all the recipe's ingredients by filling out an instance of {@link IRecipeLayoutBuilder}.
     * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
     *
     * <p>
     * Note that you can <b>only</b> add inputs and outputs for JEI's recipe lookup/search here, as the layout builder that's
     * passed into this method only handles those and not the displayed recipe previews.
     */
    @ApiStatus.OverrideOnly
    public abstract void setupRecipeIngredients(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

    private ModularScreen createScreen(T recipe) {
        ResourceLocation id = this.recipeIdGetter.apply(recipe);
        return createScreen(this.recipeUI.apply(recipe), id.getNamespace(), SCREEN_NAME_PREFIX + id.getPath(), recipe);
    }

    @ApiStatus.OverrideOnly
    public ModularScreen createScreen(IWidget recipeUI, String owner, String name, T recipe) {
        ModularPanel<?> panel;
        if (recipeUI instanceof ModularPanel<?> panel1) {
            panel = panel1;
        } else {
            panel = new ModularPanel<>(name);
            panel.coverChildren(60, 40)
                    .invisible()
                    .child(recipeUI);
        }
        ModularScreen screen = ModularScreen.createEmbed(owner, panel);
        screen.getContext().getUISettings().drawTooltipExternally(true);

        RecipeDebugDecoratorUtil.addRecipeDebugOverlays(screen);
        return screen;
    }

    @ApiStatus.OverrideOnly
    public IWidget transformWidget(IRecipeExtrasBuilder builder, IWidget widget) {
        if (!(widget instanceof JeiRecipeViewerSlot<?, ?> recipeViewerSlot)) return widget;

        if (builder instanceof RecipeLayoutAccessor accessor) {
            recipeViewerSlot.setCycler(accessor.modularui$getCycleTicker());
        }

        String name = recipeViewerSlot.getName();
        assert name != null; // the slots should always have a name assigned in createRecipeSlotForWidget()
        // the JEI slot should also always exist as it's created in the same method
        IRecipeSlotDrawable slot = builder.getRecipeSlots().findSlotByName(name).orElseThrow();

        recipeViewerSlot.setSlotWidget(slot);
        builder.addSlottedWidget(recipeViewerSlot, List.of(slot));

        return recipeViewerSlot;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public IWidget createRecipeSlotForWidget(IRecipeLayoutBuilder builder, IWidget widget, T recipe, IFocusGroup focuses, MutableInt index) {
        if (!(widget instanceof JeiRecipeViewerSlot recipeViewerSlot)) return widget;

        recipeViewerSlot.setIngredientManager(((RecipeLayoutBuilderAccessor) builder).modularui$getIngredientManager());
        recipeViewerSlot.setRecipeCategory(this);
        recipeViewerSlot.setRecipe(recipe);
        recipeViewerSlot.setFocuses(focuses);
        if (recipeViewerSlot.getName() == null) {
            recipeViewerSlot.name("jei_slot_" + index.getAndIncrement());
        }

        IRecipeSlotBuilder slotBuilder = builder.addSlot(ModularUIJeiPlugin.mapToJeiRole(recipeViewerSlot.recipeSlotRole()))
                .setSlotName(recipeViewerSlot.getName());
        recipeViewerSlot.configureJeiSlotBuilder(slotBuilder);

        // always configure a fluid renderer (it's only used if a fluid is in the slot)
        slotBuilder.setFluidRenderer(1, false, 16, 16);

        return recipeViewerSlot;
    }

    @Override
    public Component getTitle() {
        return RecipeViewerUtils.getCategoryTitle(this.getRecipeType().getUid());
    }

    // this is a map instead of a simple field so mods that make JEI loading asynchronous work as expected
    private final Map<T, ModularScreen> veryTemporaryScreenCache = new ConcurrentHashMap<>();

    @MustBeInvokedByOverriders
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        // guard against JEMI issues by explicitly checking for JEI's implementation
        // this is also done to skip having to create the whole widget tree when JEI is only looking up the recipe's ingredients
        if (builder instanceof RecipeLayoutBuilderAccessor) {
            ModularScreen screen = createScreen(recipe);
            MutableInt index = new MutableInt(0);
            screen.getMainPanel().visitTransformAllChildren(widget -> createRecipeSlotForWidget(builder, widget, recipe, focuses, index));
            veryTemporaryScreenCache.put(recipe, screen);
        } else {
            // don't bother with creating the full widget tree if setRecipe was called to get the recipe's ingredients
            this.setupRecipeIngredients(builder, recipe, focuses);
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        ModularScreen screen = veryTemporaryScreenCache.get(recipe);
        if (screen == null) {
            ModularUI.LOGGER.error("Could not get cached screen for recipe {} somehow?!", recipe);
            return;
        }
        veryTemporaryScreenCache.remove(recipe);

        screen.getMainPanel().visitTransformAllChildren(widget -> transformWidget(builder, widget));
        UIWrapperWidget wrapper = new UIWrapperWidget(screen);
        builder.addGuiEventListener(wrapper);
        builder.addWidget(wrapper);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltipBuilder, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {}

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }

    @Override
    public final int getHeight() {
        return getMaxHeight();
    }

    public int getHeight(T recipe) {
        Dimensions size = this.displaySizeCache.getUnchecked(recipe);
        return size.height;
    }

    @Override
    public final int getWidth() {
        return getMaxWidth();
    }

    public int getWidth(T recipe) {
        Dimensions display = this.displaySizeCache.getUnchecked(recipe);
        return display.width;
    }

    protected record Dimensions(int width, int height) { }

    public static class UIWrapperWidget implements IJeiGuiEventListener, IRecipeWidget {

        private final ModularScreen screen;

        public UIWrapperWidget(ModularScreen screen) {
            this.screen = screen;
        }

        public ScreenRectangle getArea() {
            return this.screen.getMainRectangle();
        }

        @Override
        public ScreenPosition getPosition() {
            return getArea().position();
        }

        @Override
        public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
            EmbedHandler.drawEmbed(this.screen, graphics, (int) mouseX, (int) mouseY, Minecraft.getInstance().getPartialTick());
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltipBuilder, double mouseX, double mouseY) {
            if (!this.screen.getContext().getUISettings().drawTooltipExternally()) {
                IRecipeWidget.super.getTooltip(tooltipBuilder, mouseX, mouseY);
                return;
            }

            IWidget hovered = screen.getContext().getTopHovered();
            if (hovered instanceof ITooltip<?> tooltip && tooltip.getTooltip() != null) {
                RichTooltip richTooltip = tooltip.getTooltip();
                if (richTooltip.autoUpdate()) richTooltip.markDirty();
                richTooltip.isEmpty(); // causes the tooltip to rebuild if necessary

                IRichTextBuilder<?> richTextBuilder = richTooltip.getRichText();
                if (richTextBuilder instanceof RichText richText) {
                    for (var line : richText.getAsText()) {
                        // scuffed conversion, but it mostly works
                        line.ifLeft(tooltipBuilder::add).ifRight(tooltipBuilder::add);
                    }
                }
            }
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
        public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
            return this.screen.keyPressed(keyCode, scanCode, modifiers);
        }
    }

}
