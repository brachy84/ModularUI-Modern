package brachy.modularui.core.mixins.jei;

import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.RecipeLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipeLayout.class, remap = false)
public interface RecipeLayoutAccessor {

    @Accessor("cycleTicker")
    CycleTicker modularui$getCycleTicker();

}
