package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeGuiLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RecipeGuiLogic.class, remap = false)
public class RecipeGuiLogicMixin {

    @ModifyVariable(method = "getVisibleRecipeLayoutsWithButtons", at = @At(value = "STORE"), name = "recipeHeight")
    private int modularui$useCategoryMaxHeightForLayout(int recipeHeight,
                                                        @Local(name = "recipeCategory") IRecipeCategory<?> recipeCategory) {
        // if this is a MUI category, force the gui to use the category's max height as the recipe height
        //  (instead of the height of the first recipe it finds, which may be shorter than others)
        if (recipeCategory instanceof ModularUIJeiCategory<?> muiCategory) {
            return muiCategory.getMaxHeight();
        }
        return recipeHeight;
    }
}
