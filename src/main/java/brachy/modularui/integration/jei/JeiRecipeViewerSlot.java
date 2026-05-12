package brachy.modularui.integration.jei;

import brachy.modularui.integration.emi.EmiRecipeViewerSlot;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;

import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.ClickableIngredient;

import mezz.jei.common.util.ImmutableRect2i;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static brachy.modularui.integration.jei.ModularUIJeiPlugin.jeiHelpers;

public class JeiRecipeViewerSlot extends RecipeViewerSlotWidget<JeiRecipeViewerSlot> {

    public JeiRecipeViewerSlot() {
        throw new NotImplementedException();
    }

    @Override
    public JeiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        return getThis();
    }

    @Override
    public JeiRecipeViewerSlot value(FluidEntryList fluidEntryList) {
        return getThis();
    }

    @Override
    public JeiRecipeViewerSlot value(ItemEntryList itemEntryList) {
        return getThis();
    }

    @Override
    public JeiRecipeViewerSlot chance(float chance) {
        return getThis();
    }
}
