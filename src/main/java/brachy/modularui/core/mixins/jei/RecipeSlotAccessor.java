package brachy.modularui.core.mixins.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = RecipeSlot.class, remap = false)
public interface RecipeSlotAccessor {

    @Accessor("role")
    @Mutable
    void modularui$setRole(RecipeIngredientRole role);

    @Accessor("cycler")
    @Mutable
    void modularui$setCycler(ICycler cycler);

    @Accessor("tooltipCallbacks")
    List<IRecipeSlotRichTooltipCallback> modularui$getTooltipCallbacks();

}

