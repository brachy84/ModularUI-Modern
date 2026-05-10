package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReiIngredientHandler {

    public static List<EntryIngredient> toReiIngredient(FluidEntryList list) {
        List<EntryIngredient> ingredients = new ArrayList<>();

        if (list instanceof FluidTagList tagList) {
            return tagList.getEntries().stream()
                    .map(FluidTagList.FluidTagEntry::stacks)
                    .map(v -> EntryIngredient.of(v
                            .map(s -> dev.architectury.fluid.FluidStack.create(s.getFluid(), s.getAmount(), s.getTag()))
                            .map(EntryStacks::of)
                            .toList()))
                    .collect(Collectors.toList());
        }

        if (list instanceof FluidStackList stackList) {
            ingredients.add(EntryIngredient.of(stackList.stream()
                    .map(s -> dev.architectury.fluid.FluidStack.create(s.getFluid(), s.getAmount(), s.getTag()))
                    .map(EntryStacks::of)
                    .toList()));
        }
        return ingredients;
    }

    public static List<EntryIngredient> toReiIngredient(ItemEntryList list) {

        List<EntryIngredient> ingredients = new ArrayList<>();

        if (list instanceof ItemTagList tagList) {
            ingredients.addAll(tagList.getEntries().stream()
                    .map(ItemTagList.ItemTagEntry::stacks)
                    .map(v -> EntryIngredient.of(v.map(EntryStacks::of).toList()))
                    .toList());
        }

        if (list instanceof ItemStackList stackList) {
            ingredients.add(EntryIngredient.of(stackList.stream().map(EntryStacks::of).toList()));
        }
        return ingredients;
    }
}
