package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("NonExtendableApiUsage")
@Mixin(value = RecipeLayoutBuilder.class, remap = false)
public abstract class RecipeLayoutBuilderMixin<T> implements IRecipeLayoutBuilder {

    @Shadow
    private @Final T recipe;
    
    private static final @Unique ThreadLocal<Object> modularui$currentRecipe = new ThreadLocal<>();
    
    @Inject(method = "buildRecipeLayout", at = @At("HEAD"))
    private void modularui$setCurrentRecipe(IFocusGroup focuses, Collection<IRecipeCategoryDecorator<T>> decorators,
                                            IScalableDrawable recipeBackground, int recipeBorderPadding,
                                            CallbackInfoReturnable<Optional<RecipeLayout<T>>> cir) {
        modularui$currentRecipe.set(this.recipe);
    }

    @Inject(method = "buildRecipeLayout", at = @At("RETURN"))
    private void modularui$clearCurrentRecipe(IFocusGroup focuses, Collection<IRecipeCategoryDecorator<T>> decorators,
                                            IScalableDrawable recipeBackground, int recipeBorderPadding,
                                            CallbackInfoReturnable<Optional<RecipeLayout<T>>> cir) {
        modularui$currentRecipe.remove();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ModifyExpressionValue(method = { "createShapelessIcon", "getRecipeTransferButtonPosition" },
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getWidth()I"), require = 0)
    private static int modularui$makeWidthRecipeDependent(int original,
                                                          @Local(argsOnly = true) IRecipeCategory recipeCategory) {
        if (recipeCategory instanceof ModularUIJeiCategory muiCategory) {
            return muiCategory.getWidth(modularui$currentRecipe.get());
        }
        return original;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ModifyExpressionValue(method = { "createShapelessIcon", "getRecipeTransferButtonPosition" },
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getHeight()I"), require = 0)
    private static int modularui$makeHeightRecipeDependent(int original,
                                                           @Local(argsOnly = true) IRecipeCategory recipeCategory) {
        if (recipeCategory instanceof ModularUIJeiCategory muiCategory) {
            return muiCategory.getHeight(modularui$currentRecipe.get());
        }
        return original;
    }
}
