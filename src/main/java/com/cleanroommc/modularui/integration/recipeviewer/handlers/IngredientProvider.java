package com.cleanroommc.modularui.integration.recipeviewer.handlers;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeSlotRole;
import com.cleanroommc.modularui.integration.recipeviewer.entry.EntryList;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for recipe viewers to get the ingredient from a widget to show recipes for example.
 * Implement this on {@link com.cleanroommc.modularui.api.widget.IWidget}.
 * No further registration needed.
 *
 * @param <I> type of the ingredient
 */
public interface IngredientProvider<I> extends IWidget {

    EntryList<I> getIngredients();

    default UnaryOperator<I> renderMappingFunction() {
        return UnaryOperator.identity();
    }

    default float chance() {
        return 1.0f;
    }

    /**
     * @return the class of the ingredient this slot contains
     */
    @NotNull
    Class<I> ingredientClass();

    /**
     * @return a recipeviewer-specific ingredient instance, or {@code null} if defaulting to the class-based logic.
     */
    @Nullable
    default Object ingredientOverride() {
        return null;
    }

    @NotNull
    default RecipeSlotRole recipeRole() {
        return RecipeSlotRole.RENDER_ONLY;
    }
}
