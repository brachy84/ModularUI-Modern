package brachy.modularui.integration.emi;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmiRecipeViewerSlot extends RecipeViewerSlotWidget<EmiRecipeViewerSlot> {

    private SlotWidget slot;
    private int x, y;

    private RecipeSlotRole recipeSlotRole;
    private EntryList<?> value;

    public EmiRecipeViewerSlot() {
        super();
        recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
    }

    @Override
    public EmiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot value(FluidEntryList fluidEntryList) {
        value = fluidEntryList;
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot value(ItemEntryList itemEntryList) {
        value = itemEntryList;
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot value(ItemStack stack) {
        value = ItemStackList.of(stack);
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot value(FluidStack stack) {
        value = FluidStackList.of(stack);
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot chance(float chance) {
        return getThis();
    }

    private void rebuildEmiSlot() {}


    public static class EmiIngredientHandler {

        public static List<EmiIngredient> toEmiIngredient(ItemEntryList list, float xeiChance,
                                                   UnaryOperator<ItemStack> realStack) {
            if (list instanceof ItemTagList tagList) {
                return tagList.getEntries().stream()
                        .map(ItemTagList.ItemTagEntry::stacks)
                        .map(stream -> toEmiIngredient(stream, realStack).setChance(xeiChance))
                        .collect(Collectors.toList());
            }
            if (list instanceof ItemStackList stackList) {
                return List.of(toEmiIngredient(stackList.stream(), realStack).setChance(xeiChance));

            }
            return Collections.emptyList();
        }

        public static List<EmiIngredient> toEmiIngredient(FluidEntryList list, float xeiChance) {
            if (list instanceof FluidTagList tagList) {
                return tagList.getEntries().stream()
                        .map(FluidTagList.FluidTagEntry::stacks)
                        .map(stream -> toEMIIngredient(stream).setChance(xeiChance))
                        .collect(Collectors.toList());
            }

            if (list instanceof FluidStackList stackList) {
                return List.of(toEMIIngredient(stackList.stream()).setChance(xeiChance));
            }

            return Collections.emptyList();
        }

        private static EmiIngredient toEmiIngredient(Stream<ItemStack> stream, UnaryOperator<ItemStack> realStack) {
            return EmiIngredient.of(stream.map(realStack).map(EmiStack::of).toList());
        }


        private static EmiIngredient toEMIIngredient(Stream<FluidStack> stream) {
            return EmiIngredient.of(stream.map(ForgeEmiStack::of).toList());
        }

    }
}
