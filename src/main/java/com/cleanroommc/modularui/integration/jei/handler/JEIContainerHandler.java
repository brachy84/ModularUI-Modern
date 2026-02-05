package com.cleanroommc.modularui.integration.jei.handler;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.integration.jei.GTJEIPlugin;
import com.cleanroommc.modularui.integration.xei.handlers.IngredientProvider;
import com.cleanroommc.modularui.screen.ContainerScreenWrapper;
import com.cleanroommc.modularui.utils.Rectangle;

import net.minecraft.client.renderer.Rect2i;

import lombok.Getter;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class JEIContainerHandler implements IGuiContainerHandler<ContainerScreenWrapper> {

    public static final JEIContainerHandler INSTANCE = new JEIContainerHandler();

    private JEIContainerHandler() {}

    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(ContainerScreenWrapper screen) {
        return screen.screen().getContext()
                .getXeiSettings().getAllExclusionAreas()
                .stream().map(Rectangle::asRect2i)
                .toList();
    }

    @Override
    public @NotNull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull ContainerScreenWrapper screen,
                                                                                       double mouseX, double mouseY) {
        IGuiElement hovered = screen.screen().getContext().getTopHovered();
        if (hovered instanceof IngredientProvider<?> provider) {
            var override = provider.ingredientOverride();
            if (override != null) {
                JEIScreenHandler.currentIngredient = ((IClickableIngredient<?>) override).getTypedIngredient();
                return Optional.of((IClickableIngredient<?>) override);
            }
            if (provider.getIngredients().isEmpty()) return Optional.empty();

            Optional<? extends ITypedIngredient<?>> ingredient = GTJEIPlugin.getRuntime()
                    .getIngredientManager()
                    .createTypedIngredient(mapFirstIngredient(provider));

            JEIScreenHandler.currentIngredient = ingredient.orElse(null);
            return ingredient.map(i -> new ClickableIngredient<>(i, hovered.getArea().asRect2i()));
        }
        return Optional.empty();
    }

    private <T> T mapFirstIngredient(IngredientProvider<T> provider) {
        return provider.renderMappingFunction().apply(provider.getIngredients().getStacks().get(0));
    }

    private record ClickableIngredient<T>(ITypedIngredient<T> ingredient, @Getter Rect2i area)
            implements IClickableIngredient<T> {

        @SuppressWarnings("removal") // I have to override this.
        @Override
        public @NotNull ITypedIngredient<T> getTypedIngredient() {
            return ingredient;
        }

        @Override
        public @NotNull IIngredientType<T> getIngredientType() {
            return ingredient.getType();
        }

        @Override
        public @NotNull T getIngredient() {
            return ingredient.getIngredient();
        }
    }
}
