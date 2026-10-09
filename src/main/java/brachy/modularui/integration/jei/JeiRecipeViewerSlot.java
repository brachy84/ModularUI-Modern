package brachy.modularui.integration.jei;

import brachy.modularui.core.mixins.jei.RecipeSlotAccessor;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.screen.viewport.ModularGuiContext;

import brachy.modularui.theme.WidgetThemeEntry;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.recipes.OutputSlotTooltipCallback;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static brachy.modularui.integration.jei.ModularUIJeiPlugin.mapToJeiRole;

@ApiStatus.Internal
public class JeiRecipeViewerSlot<I, T> extends RecipeViewerSlotWidget<I, JeiRecipeViewerSlot<I, T>> implements ISlottedRecipeWidget {

    @Getter private @Nullable IRecipeSlotDrawable slotWidget;
    @Setter private IFocusGroup focuses;
    @Setter private ICycler cycler;

    @Setter private IRecipeCategory<T> recipeCategory;
    @Setter private T recipe;

    @Setter private IIngredientManager ingredientManager;

    public JeiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);

        size(18, 18);
        tooltipAutoUpdate(true);
        tooltipDynamic(tooltip -> {
            if (slotWidget != null){
                for (Component component : slotWidget.getTooltip()){
                    tooltip.addLine(component);
                }
            }
        });
    }

    public void setSlotWidget(IRecipeSlotDrawable slotWidget) {
        this.slotWidget = slotWidget;
        if (this.entries != null) rebuildRealSlot();
    }

    /**
     * Instead of creating a new slot, the JEI implementation of {@code RecipeViewerSlotWidget} (this class) overwrites most of the {@linkplain mezz.jei.library.gui.ingredients.RecipeSlot JEI recipe slot's} values with new ones.<br>
     * It does this because JEI keeps track of all recipe slots & it's easier to replace existing slots' data than it is to replace the slots themselves.
     */
    @Override
    protected void rebuildRealSlot() {
        // only assign all of these values once the slot has been created from JEI's side.
        if (!(slotWidget instanceof RecipeSlotAccessor recipeSlot)) {
            // null check and cast in one!
            return;
        }

        RecipeIngredientRole jeiIngredientRole = mapToJeiRole(this.recipeSlotRole);
        if (slotWidget.getRole() != jeiIngredientRole) {
            // always remove the output slot tooltip callback (this is easier than checking if it already exists etc.)
            recipeSlot.modularui$getTooltipCallbacks().removeIf(callback -> callback instanceof OutputSlotTooltipCallback);
            if (this.recipeSlotRole == RecipeSlotRole.OUTPUT) {
                // (re)add the output slot tooltip callback if this is now an output slot
                addOutputSlotTooltipCallback(recipeSlot);
            }
            recipeSlot.modularui$setRole(jeiIngredientRole);
        }
        // mmm I love off-by-one errors
        slotWidget.setPosition(1, 1);

        replaceSlotIngredients(recipeSlot, jeiIngredientRole);

        recipeSlot.modularui$setCycler(this.cycler);
    }

    @ApiStatus.Internal
    public void configureJeiSlotBuilder(IRecipeSlotBuilder builder) {
        if (this.entries != null) {
            builder.addIngredientsUnsafe(this.entries.getStacks().stream()
                    .map(this.renderMappingFunction)
                    .toList());
        }
        // mmm I love off-by-one errors
        builder.setPosition(1, 1);
    }

    // Mostly copied from RecipeSlotBuilder#build
    private void replaceSlotIngredients(RecipeSlotAccessor recipeSlot, RecipeIngredientRole ingredientRole) {
        if (this.ingredientManager == null) {
            if (ModularUIJeiPlugin.jeiHelpers == null) {
                return;
            }
            this.ingredientManager = ModularUIJeiPlugin.jeiHelpers.getIngredientManager();
        }

        final DisplayIngredientAcceptor ingredients = new DisplayIngredientAcceptor(this.ingredientManager);
        ingredients.addIngredientsUnsafe(this.entries.getStacks().stream()
                .map(this.renderMappingFunction)
                .toList());

        List<Optional<ITypedIngredient<?>>> allIngredients = ingredients.getAllIngredients();

        // recipeSlot is the same object as this.slotWidget
        // noinspection DataFlowIssue
        if (!slotWidget.isEmpty()) {
            // check if the slot's ingredients are the same as our new ones and skip replacing them if so
            // this lets us optimize lookup by not parsing the focused ingredients if possible
            Set<ITypedIngredient<?>> newIngredients = allIngredients.stream().flatMap(Optional::stream).collect(Collectors.toSet());
            slotWidget.getAllIngredients().forEach(newIngredients::remove);

            // every ingredient was removed -> the ingredient lists are equal
            if (newIngredients.isEmpty()) {
                return;
            }
        }

        IntSet focusMatches = ingredients.getMatches(this.focuses, ingredientRole);
        List<Optional<ITypedIngredient<?>>> focusedIngredients = null;
        if (!focusMatches.isEmpty()) {
            focusedIngredients = new ArrayList<>();
            for (IntIterator iterator = focusMatches.iterator(); iterator.hasNext(); ) {
                int i = iterator.nextInt();
                if (i < allIngredients.size()) {
                    Optional<ITypedIngredient<?>> ingredient = allIngredients.get(i);
                    focusedIngredients.add(ingredient);
                }
            }
        }

        recipeSlot.modularui$setAllIngredients(allIngredients);
        recipeSlot.modularui$setDisplayIngredients(focusedIngredients);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.slotWidget == null) return;

        this.slotWidget.draw(context.getGraphics());
        if (isHovering()){
            slotWidget.drawHoverOverlays(context.getGraphics());
        }
    }

/*
    @Override
    public Result onMousePressed(int button) {
        return this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getMouseY(), button) ? Result.SUCCESS : Result.ACCEPT;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
*/

    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (isHovering() && slotWidget != null) {
            return Optional.of(new RecipeSlotUnderMouse(slotWidget, getArea().x, getArea().y));
        }
        return Optional.empty();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(getArea().x, getArea().y);
    }

    // disable JEI draw functionality
    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {}

    // copied from RecipeLayoutBuilder#addOutputSlotTooltipCallback
    private void addOutputSlotTooltipCallback(RecipeSlotAccessor slot) {
        ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
        if (recipeName != null) {
            RecipeType<T> recipeType = recipeCategory.getRecipeType();
            OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName, recipeType);
            slot.modularui$getTooltipCallbacks().add(callback);
        }
    }
}
