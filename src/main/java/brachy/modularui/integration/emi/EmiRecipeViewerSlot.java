package brachy.modularui.integration.emi;

import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;

import dev.emi.emi.api.widget.TankWidget;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.ApiStatus;

public class EmiRecipeViewerSlot extends RecipeViewerSlotWidget<EmiRecipeViewerSlot> {

    @ApiStatus.Internal
    @Getter
    private SlotWidget slotWidget;
    private int x, y;

    @Accessors(fluent = true)
    @Getter
    private RecipeSlotRole recipeSlotRole;
    private EntryList<?> value;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private float chance = 1f;

    public EmiRecipeViewerSlot() {
        super();
        slotWidget = new SlotWidget(EmiIngredient.of(Ingredient.EMPTY), 0, 0);
        recipeSlotRole = RecipeSlotRole.RENDER_ONLY;

        size(18, 18);

        tooltipAutoUpdate(true);
        tooltipDynamic(tooltip -> {
            for (ClientTooltipComponent ctc : this.slotWidget.getTooltip(getContext().getAbsMouseX(), getContext().getAbsMouseY())) {
                tooltip.addDrawableLine(new ClientTooltipComponentIcon(ctc));
            }
        });
    }

    @Override
    public EmiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        slotWidget.catalyst(recipeSlotRole == RecipeSlotRole.CATALYST);
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot value(FluidEntryList fluidEntryList) {
        value = fluidEntryList;
        rebuildEmiSlot();
        background(GuiTextures.SLOT_FLUID);
        return getThis();
    }

    @Override
    public EmiRecipeViewerSlot value(ItemEntryList itemEntryList) {
        value = itemEntryList;
        rebuildEmiSlot();
        background(GuiTextures.SLOT_ITEM);
        return getThis();
    }

    private void rebuildEmiSlot() {
        if (value instanceof ItemEntryList itemEntryList) {
            slotWidget = new SlotWidget(EmiStackConverter.ITEM.convertTo(itemEntryList, chance), 0, 0);
        } else if (value instanceof FluidEntryList fluidEntryList) {
            slotWidget = new TankWidget(EmiStackConverter.FLUID.convertTo(fluidEntryList, chance), 0, 0, 18, 18, 1);
        }
        slotWidget.drawBack(false);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        context.getGraphics().pose().translate(-this.x, -this.y, 0);
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        context.getGraphics().pose().translate(this.x, this.y, 0);
    }

    @Override
    public Result onMousePressed(int button) {
        this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getAbsMouseY(), button);
        return Result.SUCCESS;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
}
