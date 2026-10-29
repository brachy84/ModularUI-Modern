package brachy.modularui.integration.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.gui.recipes.OutputSlotTooltipCallback;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Compatibility helpers for JEI's internal RecipeSlot ingredient layout.
 */
final class JeiRecipeSlotCompat {

    private JeiRecipeSlotCompat() {}

    static void replaceIngredients(Object recipeSlot, List<?> allIngredients, List<?> displayIngredients) {
        Field ingredientsField = findField(recipeSlot.getClass(), "ingredients");
        try {
            if (ingredientsField != null) {
                Constructor<?> constructor = ingredientsField.getType()
                        .getDeclaredConstructor(List.class, List.class, Runnable.class);
                constructor.setAccessible(true);
                Object ingredients = constructor.newInstance(allIngredients, displayIngredients, (Runnable) () -> {});
                ingredientsField.setAccessible(true);
                ingredientsField.set(recipeSlot, ingredients);
                return;
            }

            setField(recipeSlot, "allIngredients", allIngredients);
            setField(recipeSlot, "displayIngredients", displayIngredients);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unsupported JEI RecipeSlot ingredient layout", exception);
        }
    }

    static IRecipeSlotRichTooltipCallback createOutputSlotTooltipCallback(ResourceLocation recipeId,
                                                                           RecipeType<?> recipeType) {
        try {
            Constructor<?> constructor;
            Object[] arguments;
            try {
                constructor = OutputSlotTooltipCallback.class
                        .getConstructor(ResourceLocation.class, RecipeType.class);
                arguments = new Object[] { recipeId, recipeType };
            } catch (NoSuchMethodException olderJei) {
                constructor = OutputSlotTooltipCallback.class
                        .getConstructor(ResourceLocation.class, List.class, RecipeType.class);
                arguments = new Object[] { recipeId, List.of(recipeId), recipeType };
            }
            return (IRecipeSlotRichTooltipCallback) constructor.newInstance(arguments);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unsupported JEI output tooltip callback API", exception);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws IllegalAccessException {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            throw new IllegalStateException("JEI RecipeSlot is missing field '" + fieldName + "'");
        }
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // Continue in case JEI moves the field into a parent class.
            }
        }
        return null;
    }
}

