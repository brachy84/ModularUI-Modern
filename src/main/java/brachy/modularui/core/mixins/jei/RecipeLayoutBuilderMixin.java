package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeLayoutBuilder.class, remap = false)
public abstract class RecipeLayoutBuilderMixin<T> implements IRecipeLayoutBuilder {

    @Shadow
    private @Final T recipe;

    @Shadow
    private @Final IRecipeCategory<T> recipeCategory;

    @ModifyExpressionValue(method = { "createShapelessIcon", "getRecipeTransferButtonPosition" },
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getWidth()I"), require = 0)
    private int modularui$makeWidthRecipeDependent(int original) {
        if (recipeCategory instanceof ModularUIJeiCategory<T> muiCategory) {
            return muiCategory.getWidth(this.recipe);
        }
        return original;
    }

    @ModifyExpressionValue(method = { "createShapelessIcon", "getRecipeTransferButtonPosition" },
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getHeight()I"), require = 0)
    private int modularui$makeHeightRecipeDependent(int original) {
        if (recipeCategory instanceof ModularUIJeiCategory<T> muiCategory) {
            return muiCategory.getHeight(recipe);
        }
        return original;
    }
}
