package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.gui.recipes.RecipeLayout;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = RecipeLayout.class, remap = false)
public abstract class RecipeLayoutMixin<R> implements IRecipeLayoutDrawable<R> {

    @WrapOperation(method = "drawOverlays",
                   at = @At(value = "INVOKE", target = "Lmezz/jei/library/gui/recipes/RecipeLayout;getSlotUnderMouse(DD)Ljava/util/Optional;"))
    private Optional<RecipeSlotUnderMouse> modularui$disableIngredientTooltipRender(RecipeLayout<R> self, double mouseX, double mouseY,
                                                                                    Operation<Optional<RecipeSlotUnderMouse>> original) {
        if (self.getRecipeCategory() instanceof ModularUIJeiCategory<R>){
            return Optional.empty();
        }
        return original.call(self, mouseX, mouseY);
    }

    @ModifyExpressionValue(method = "<init>",
                           at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getWidth()I"),
                           require = 0)
    private int modularui$makeWidthRecipeDependent(int original,
                                                   @Local(argsOnly = true) IRecipeCategory<R> recipeCategory,
                                                   @Local(argsOnly = true) R recipe) {
        if (recipeCategory instanceof ModularUIJeiCategory<R> muiCategory) {
            return muiCategory.getWidth(recipe);
        }
        return original;
    }

    @ModifyExpressionValue(method = "<init>",
                           at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getHeight()I"),
                           require = 0)
    private int modularui$makeHeightRecipeDependent(int original,
                                                    @Local(argsOnly = true) IRecipeCategory<R> recipeCategory,
                                                    @Local(argsOnly = true) R recipe) {
        if (recipeCategory instanceof ModularUIJeiCategory<R> muiCategory) {
            return muiCategory.getHeight(recipe);
        }
        return original;
    }
}
