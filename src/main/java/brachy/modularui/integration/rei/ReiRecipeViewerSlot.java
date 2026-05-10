package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;

import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;

import org.apache.commons.lang3.NotImplementedException;

public class ReiRecipeViewerSlot extends RecipeViewerSlotWidget<ReiRecipeViewerSlot> {

    public ReiRecipeViewerSlot() {
        throw new NotImplementedException();
    }

    @Override
    public ReiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        return getThis();
    }

    @Override
    public ReiRecipeViewerSlot value(FluidEntryList fluidEntryList) {
        return getThis();
    }

    @Override
    public ReiRecipeViewerSlot value(ItemEntryList itemEntryList) {
        return getThis();
    }

    @Override
    public ReiRecipeViewerSlot chance(float chance) {
        return getThis();
    }
}
