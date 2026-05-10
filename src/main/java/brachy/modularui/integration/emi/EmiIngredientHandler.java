package brachy.modularui.integration.emi;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;

import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class EmiIngredientHandler {

    public static EmiIngredient toEmiIngredient(ItemEntryList list, float ingredientChance) {
        List<EmiIngredient> ingredients = new ArrayList<>();
        if (list instanceof ItemTagList tagList) {
            ingredients.addAll(tagList.getEntries().stream()
                    .map(ItemTagList.ItemTagEntry::stacks)
                    .map(stream -> EmiIngredient.of(stream.map(EmiStack::of).toList()).setChance(ingredientChance)).toList());
        }
        if (list instanceof ItemStackList stackList) {
            ingredients.add(EmiIngredient.of(stackList.stream().map(EmiStack::of).toList()).setChance(ingredientChance));
        }

        if (ingredients.isEmpty()) return EmiIngredient.of(Ingredient.EMPTY);
        if (ingredients.size() == 1) return ingredients.get(0);
        return EmiIngredient.of(ingredients);
    }

    public static EmiIngredient toEmiIngredient(FluidEntryList list, float ingredientChance) {

        List<EmiIngredient> ingredients = new ArrayList<>();

        if (list instanceof FluidTagList tagList) {
            ingredients.addAll(tagList.getEntries().stream()
                    .map(FluidTagList.FluidTagEntry::stacks)
                    .map(stream -> EmiIngredient.of(stream.map(ForgeEmiStack::of).toList()).setChance(ingredientChance))
                    .toList()
            );
        }

        if (list instanceof FluidStackList stackList) {
            ingredients.add(EmiIngredient.of(stackList.stream().map(ForgeEmiStack::of).toList()).setChance(ingredientChance));
        }

        if (ingredients.isEmpty()) return EmiIngredient.of(Ingredient.EMPTY);
        if (ingredients.size() == 1) return ingredients.get(0);
        return EmiIngredient.of(ingredients);
    }
}
