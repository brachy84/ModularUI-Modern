package brachy.modularui.integration.jei;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;

import brachy.modularui.screen.viewport.ModularGuiContext;

import brachy.modularui.theme.WidgetThemeEntry;

import lombok.Getter;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A widget that draws a recipe slot built by JEI.
 * Ingredient changes after building will only change the displayed ingredients.
 */
@ApiStatus.Internal
public class JeiRecipeViewerSlot<I, T> extends RecipeViewerSlotWidget<I, JeiRecipeViewerSlot<I, T>> implements ISlottedRecipeWidget {

    @Getter private @Nullable IRecipeSlotDrawable slotWidget;
    private boolean configured;
    private boolean overrideDisplay;

    public JeiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);

        size(18, 18);
        tooltipAutoUpdate(true);
        tooltipDynamic(tooltip -> {
            if (slotWidget != null){
                for (Component component : slotWidget.getTooltip()){
                    tooltip.addLine(component);
                }
            }
        });
    }

    public void setSlotWidget(IRecipeSlotDrawable slotWidget) {
        this.slotWidget = slotWidget;
        if (this.entries != null) rebuildRealSlot();
    }

    @Override
    public JeiRecipeViewerSlot<I, T> value(EntryList<I> entryList) {
        if (this.configured || this.slotWidget != null) {
            this.overrideDisplay = true;
        }
        return super.value(entryList);
    }

    @Override
    protected void rebuildRealSlot() {
        if (!this.overrideDisplay || this.slotWidget == null || this.entries == null) return;

        // Clear the previous override because the ingredient consumer appends each update.
        this.slotWidget.clearDisplayOverrides();
        this.slotWidget.createDisplayOverrides().addIngredientsUnsafe(this.entries.getStacks().stream()
                .map(this.renderMappingFunction)
                .toList());
    }

    @ApiStatus.Internal
    public void configureJeiSlotBuilder(IRecipeSlotBuilder builder) {
        if (this.entries != null) {
            builder.addIngredientsUnsafe(this.entries.getStacks().stream()
                    .map(this.renderMappingFunction)
                    .toList());
        }
        // mmm I love off-by-one errors
        builder.setPosition(1, 1);
        this.configured = true;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.slotWidget == null) return;

        this.slotWidget.draw(context.getGraphics());
        if (isHovering()){
            slotWidget.drawHoverOverlays(context.getGraphics());
        }
    }

/*
    @Override
    public Result onMousePressed(int button) {
        return this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getMouseY(), button) ? Result.SUCCESS : Result.ACCEPT;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
*/

    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (isHovering() && slotWidget != null) {
            return Optional.of(new RecipeSlotUnderMouse(slotWidget, getArea().x, getArea().y));
        }
        return Optional.empty();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(getArea().x, getArea().y);
    }

    // disable JEI draw functionality
    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {}
}
