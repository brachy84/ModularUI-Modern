package brachy.modularui.integration.recipeviewer;

import brachy.modularui.ModularUI;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.integration.emi.EmiRecipeViewerSlot;
import brachy.modularui.integration.jei.JeiRecipeViewerSlot;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.rei.ReiRecipeViewerSlot;
import brachy.modularui.widget.Widget;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.NotImplementedException;

public abstract class RecipeViewerSlotWidget<T extends RecipeViewerSlotWidget<T>> extends Widget<T> implements Interactable {

    public abstract T recipeSlotRole(RecipeSlotRole recipeSlotRole);

    public abstract T value(FluidEntryList fluidEntryList);
    public abstract T value(ItemEntryList itemEntryList);

    public T value(ItemStack stack) {
        return value(ItemStackList.of(stack));
    }

    public T value(FluidStack stack) {
        return value(FluidStackList.of(stack));
    }

    public abstract T chance(float chance);

    public static RecipeViewerSlotWidget<?> create() {
        if (!ModularUI.Mods.isRecipeViewerLoaded()) throw new IllegalStateException("Cannot create recipe viewer slot without a recipe viewer mod loaded.");

        if (ModularUI.Mods.EMI.isLoaded()) {
            return new EmiRecipeViewerSlot();
        } else if (ModularUI.Mods.REI.isLoaded()) {
            return new ReiRecipeViewerSlot();
        } else if (ModularUI.Mods.JEI.isLoaded()) {
            return new JeiRecipeViewerSlot();
        }
        throw new UnsupportedOperationException("Cannot create recipe viewer slot without EMI, REI, or JEI being loaded.");
    }
}
