package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;

import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReiRecipeViewerSlot {

    public static class ReiIngredientHandler {

        private static EntryIngredient toReiIngredient(Stream<FluidStack> stream) {
            return EntryIngredient.of(stream
                    .map(s -> dev.architectury.fluid.FluidStack.create(s.getFluid(), s.getAmount(), s.getTag()))
                    .map(EntryStacks::of)
                    .toList());
        }

        private static EntryIngredient toReiIngredient(Stream<ItemStack> stream, UnaryOperator<ItemStack> realStack) {
            return EntryIngredient.of(stream.map(realStack)
                    .map(EntryStacks::of)
                    .toList());
        }

        public static List<EntryIngredient> toReiIngredient(FluidEntryList list) {
            if (list instanceof FluidTagList tagList) {
                return tagList.getEntries().stream()
                        .map(FluidTagList.FluidTagEntry::stacks)
                        .map(ReiIngredientHandler::toReiIngredient)
                        .collect(Collectors.toList());
            }

            if (list instanceof FluidStackList stackList) {
                return List.of(toReiIngredient(stackList.stream()));
            }
            return Collections.emptyList();
        }

        public static List<EntryIngredient> toReiIngredient(ItemEntryList list, UnaryOperator<ItemStack> realStack) {
            if (list instanceof ItemTagList tagList) {
                return tagList.getEntries().stream()
                        .map(ItemTagList.ItemTagEntry::stacks)
                        .map(stream -> toReiIngredient(stream, realStack))
                        .collect(Collectors.toList());

            }
            if (list instanceof ItemStackList stackList) {
                return List.of(toReiIngredient(stackList.stream(), realStack));
            }
            return Collections.emptyList();
        }
    }
}
