package brachy.modularui.core.mixins.emi;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiDrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiRenderHelper.class, remap = false)
public class EmiRenderHelperMixin {

    @Inject(method = "renderRecipe",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/recipe/EmiRecipe;addWidgets(Ldev/emi/emi/api/widget/WidgetHolder;)V"))
    private static void modularui$tellTheRecipeItsInATooltip(EmiRecipe recipe, EmiDrawContext context, int x, int y,
                                                             boolean showMissing, int overlayColor,
                                                             CallbackInfo ci) {
        if (recipe instanceof ModularUIEmiRecipe muiRecipe) {
            muiRecipe.useScreenCacheForNextWidgetQuery();
        }
    }
}
