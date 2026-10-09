package brachy.modularui.integration.emi.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.integration.recipeviewer.RecipeViewerUtils;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;

import java.util.Comparator;

public class ModularUIEmiCategory extends EmiRecipeCategory {

    public ModularUIEmiCategory(ResourceLocation id, EmiRenderable icon) {
        super(id, icon);
    }

    public ModularUIEmiCategory(ResourceLocation id, EmiRenderable icon, EmiRenderable simplified) {
        super(id, icon, simplified);
    }

    public ModularUIEmiCategory(ResourceLocation id, EmiRenderable icon, EmiRenderable simplified, Comparator<EmiRecipe> sorter) {
        super(id, icon, simplified, sorter);
    }

    /**
     * Override the default translation key to a more recipeviewer-agnostic one
     */
    @Override
    public Component getName() {
        return RecipeViewerUtils.getCategoryTitle(this.getId());
    }
}
