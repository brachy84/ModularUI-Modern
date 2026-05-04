package brachy.modularui.integration.jei;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;

import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.ClickableIngredient;

import mezz.jei.common.util.ImmutableRect2i;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static brachy.modularui.integration.jei.ModularUIJeiPlugin.jeiHelpers;

public class JeiRecipeViewerSlot {

    public static class JeiIngredientHandler {

        public static List<Object> toJeiIngredient(FluidEntryList list) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(JeiIngredientHandler::getJEIFluid)
                    .toList();
        }

        public static List<Object> toJeiIngredientClickable(FluidEntryList list, int x, int y, int w, int h) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(stack -> getJEIFluidClickable(stack, x, y, w, h))
                    .toList();
        }

        public static Object getJEIFluid(FluidStack fluidStack) {
            return jeiHelpers.getPlatformFluidHelper().create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
        }

        public static @Nullable Object getJEIFluidClickable(FluidStack fluidStack, int x, int y, int w, int h) {
            var ingredient = jeiHelpers.getPlatformFluidHelper().create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
            return jeiHelpers.getIngredientManager().createTypedIngredient(ingredient)
                    .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, new ImmutableRect2i(x, y, w, h)))
                    .orElse(null);
        }

        public static @Nullable Object getJEIStackClickable(ItemStack stack, int x, int y, int w, int h) {
            IIngredientManager ingredientManager = jeiHelpers.getIngredientManager();
            return ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack)
                    .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, new ImmutableRect2i(x, y, w, h)))
                    .orElse(null);
        }

        public static List<Object> toJeiIngredient(ItemEntryList list, UnaryOperator<ItemStack> realStack) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(realStack)
                    .collect(Collectors.toList());
        }

        public static List<Object> toJeiIngredientClickable(ItemEntryList list, int x, int y, int w, int h,
                                                              UnaryOperator<ItemStack> realStack) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(realStack)
                    .map(stack -> getJEIStackClickable(stack, x, y, w, h))
                    .collect(Collectors.toList());
        }

    }
}
