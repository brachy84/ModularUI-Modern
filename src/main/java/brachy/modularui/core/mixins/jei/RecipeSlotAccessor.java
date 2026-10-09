package brachy.modularui.core.mixins.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

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

    @Accessor("allIngredients")
    @Mutable
    void modularui$setAllIngredients(List<Optional<ITypedIngredient<?>>> allIngredients);

    @Accessor("displayIngredients")
    @Mutable
    void modularui$setDisplayIngredients(List<Optional<ITypedIngredient<?>>> displayIngredients);
}
