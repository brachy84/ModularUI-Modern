package com.cleanroommc.modularui.integration.jei.recipe;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeSlotRole;
import com.cleanroommc.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.cleanroommc.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.WidgetUtil;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class ModularUIRecipeCategory<T extends Recipe<?>, W extends IWidget> implements IRecipeCategory<T> {

    private final LoadingCache<T, ModularScreen> modularScreenCache;

    protected ModularUIRecipeCategory(Function<T, W> wrapperFunction, Function<T, ResourceLocation> recipeIdGetter) {
        this.modularScreenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .maximumSize(10)
                .build(new CacheLoader<>() {

                    @Override
                    public ModularScreen load(T recipe) {
                        W widget = wrapperFunction.apply(recipe);
                        ResourceLocation recipeId = recipeIdGetter.apply(recipe);

                        ModularPanel panel = ModularPanel.defaultPanel(recipeId.toString(),
                                widget.getArea().width, widget.getArea().height);
                        panel.child(widget);
                        return new ModularScreen(recipeId.getNamespace(), panel);
                    }
                });
    }

    private ModularScreen getModularScreen(T recipe) {
        return this.modularScreenCache.getUnchecked(recipe);
    }

    private static <T> void addJEISlot(IRecipeLayoutBuilder builder, IngredientProvider<T> widget,
                                       RecipeIngredientRole role, int index) {
        var type = ModularUIJeiPlugin.getRuntime().getIngredientManager()
                .getIngredientTypeChecked(widget.ingredientClass());
        if (type.isEmpty()) {
            return;
        }

        Area widgetArea = widget.getArea();
        IRecipeSlotBuilder slotBuilder = builder.addSlot(role, widgetArea.x, widgetArea.y);

        slotBuilder.addIngredients(type.get(), widget.getIngredients().getStacks());
        slotBuilder.setCustomRenderer(type.get(), new IIngredientRenderer<>() {

            @Override
            public void render(GuiGraphics guiGraphics, T ingredient) {}

            @SuppressWarnings("removal")
            @Override
            public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
                return Collections.emptyList();
            }

            @Override
            public int getWidth() {
                return widgetArea.width;
            }

            @Override
            public int getHeight() {
                return widgetArea.height;
            }
        });
        // set slot name
        slotBuilder.setSlotName("slot_" + index);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        ModularScreen screen = getModularScreen(recipe);

        int i = 0;
        for (IWidget widget : WidgetUtil.getFlatWidgetCollection(screen)) {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                continue;
            }
            RecipeIngredientRole role = mapToRole(provider.recipeRole());
            addJEISlot(builder, provider, role, i);
            i++;
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        builder.addGuiEventListener(new ModularUIGuiEventListener(recipe));
        builder.addWidget(new UIForegroundRenderWidget(recipe));
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        ModularScreen screen = getModularScreen(recipe);

        RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, (int) mouseX, (int) mouseY,
                Minecraft.getInstance().getPartialTick());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // tooltip.clear();
    }

    public static RecipeIngredientRole mapToRole(RecipeSlotRole slotRole) {
        return switch (slotRole) {
            case INPUT -> RecipeIngredientRole.INPUT;
            case OUTPUT -> RecipeIngredientRole.OUTPUT;
            case CATALYST -> RecipeIngredientRole.CATALYST;
            case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
        };
    }

    public class ModularUIGuiEventListener implements IJeiGuiEventListener {

        private final T recipe;

        public ModularUIGuiEventListener(T recipe) {
            this.recipe = recipe;
        }

        public ScreenRectangle getArea() {
            return getModularScreen(this.recipe).getRectangle();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            getModularScreen(this.recipe).mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return getModularScreen(this.recipe).mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
            return getModularScreen(this.recipe).mouseScrolled(mouseX, mouseY, scrollDelta);
        }

        @Override
        public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
            return getModularScreen(this.recipe).keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public class UIForegroundRenderWidget implements IRecipeWidget {

        private final T recipe;

        public UIForegroundRenderWidget(T recipe) {
            this.recipe = recipe;
        }

        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(0, 0);
        }

        @Override
        public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            ModularScreen screen = getModularScreen(this.recipe);
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, (int) mouseX, (int) mouseY,
                    Minecraft.getInstance().getPartialTick());
        }
    }
}
