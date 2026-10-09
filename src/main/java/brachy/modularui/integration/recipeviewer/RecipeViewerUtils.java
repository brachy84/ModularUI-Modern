package brachy.modularui.integration.recipeviewer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class RecipeViewerUtils {

    private RecipeViewerUtils() {}

    public static final String CATEGORY_LANGUAGE_KEY_PREFIX = "gui.recipeviewer.category";

    public static Component getCategoryTitle(ResourceLocation categoryId) {
        return Component.translatable(categoryId.toLanguageKey(CATEGORY_LANGUAGE_KEY_PREFIX));
    }

}
