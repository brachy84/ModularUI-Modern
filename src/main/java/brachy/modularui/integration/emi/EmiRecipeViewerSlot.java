package brachy.modularui.integration.emi;

import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.theme.WidgetThemeEntry;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EmiRecipeViewerSlot<I> extends RecipeViewerSlotWidget<I, EmiRecipeViewerSlot<I>> {

    @Getter private SlotWidget slotWidget;

    public EmiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);
        this.slotWidget = new SlotWidget(EmiIngredient.of(Ingredient.EMPTY), 0, 0);

        size(18, 18);

        tooltipAutoUpdate(true);
        tooltipDynamic(tooltip -> {
            for (ClientTooltipComponent ctc : this.slotWidget.getTooltip(getContext().getAbsMouseX(), getContext().getAbsMouseY())) {
                tooltip.addDrawableLine(new ClientTooltipComponentIcon(ctc));
            }
        });
    }

    @Override
    protected void rebuildRealSlot() {
        EmiIngredient ingredient = EmiStackConverter.convertToEmiEntry(this.entries, this.chance, this.renderMappingFunction);
        if (this.ingredientClass == FluidStack.class) {
            // special case fluid slots
            slotWidget = new TankWidget(ingredient, 0, 0, 18, 18, 1);
        } else {
            slotWidget = new SlotWidget(ingredient, 0, 0);
        }
        slotWidget.drawBack(false);
        slotWidget.catalyst(recipeSlotRole == RecipeSlotRole.CATALYST);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
    }

    @Override
    public Result onMousePressed(int button) {
        return this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getMouseY(), button) ? Result.SUCCESS : Result.ACCEPT;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
}
