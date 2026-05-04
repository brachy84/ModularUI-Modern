package brachy.modularui.integration.emi;

import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;

import dev.emi.emi.api.widget.TankWidget;

import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class EmiRecipeViewerSlot extends RecipeViewerSlotWidget<EmiRecipeViewerSlot> {

    @ApiStatus.Internal
    @Getter
    private SlotWidget slotWidget;
    private int x, y;

    @Accessors(fluent = true)
    @Getter
    private RecipeSlotRole recipeSlotRole;
    private EntryList<?> value;
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

    @Override
    public EmiRecipeViewerSlot chance(float chance) {
        this.chance = chance;
        return getThis();
    }

    private void rebuildEmiSlot() {
        if (value instanceof ItemEntryList itemEntryList) {
            slotWidget = new SlotWidget(EmiIngredientHandler.toEmiIngredient(itemEntryList, chance, UnaryOperator.identity()), 0, 0);
        } else {
            slotWidget = new TankWidget(EmiIngredientHandler.toEmiIngredient((FluidEntryList)value, chance), 0, 0, 18, 18, 1);
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

    public static class EmiIngredientHandler {

        public static EmiIngredient toEmiIngredient(ItemEntryList list, float xeiChance,
                                                   UnaryOperator<ItemStack> realStack) {
            List<EmiIngredient> ingredients = new ArrayList<>();
            if (list instanceof ItemTagList tagList) {
                ingredients.addAll(tagList.getEntries().stream()
                        .map(ItemTagList.ItemTagEntry::stacks)
                        .map(stream -> EmiIngredient.of(stream.map(realStack).map(EmiStack::of).toList()).setChance(xeiChance)).toList());
            }
            if (list instanceof ItemStackList stackList) {
                ingredients.add(EmiIngredient.of(stackList.stream().map(realStack).map(EmiStack::of).toList()).setChance(xeiChance));
            }

            if (ingredients.isEmpty()) return EmiIngredient.of(Ingredient.EMPTY);
            if (ingredients.size() == 1) return ingredients.get(0);
            return EmiIngredient.of(ingredients);
        }

        public static EmiIngredient toEmiIngredient(FluidEntryList list, float xeiChance) {

            List<EmiIngredient> ingredients = new ArrayList<>();

            if (list instanceof FluidTagList tagList) {
                ingredients.addAll(tagList.getEntries().stream()
                        .map(FluidTagList.FluidTagEntry::stacks)
                        .map(stream -> EmiIngredient.of(stream.map(ForgeEmiStack::of).toList()).setChance(xeiChance))
                        .toList()
                );
            }

            if (list instanceof FluidStackList stackList) {
                ingredients.add(EmiIngredient.of(stackList.stream().map(ForgeEmiStack::of).toList()).setChance(xeiChance));
            }

            if (ingredients.isEmpty()) return EmiIngredient.of(Ingredient.EMPTY);
            if (ingredients.size() == 1) return ingredients.get(0);
            return EmiIngredient.of(ingredients);
        }
    }
}
