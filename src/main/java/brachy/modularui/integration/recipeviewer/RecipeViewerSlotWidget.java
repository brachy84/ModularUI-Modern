package brachy.modularui.integration.recipeviewer;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

@Accessors(fluent = true)
@ApiStatus.Experimental
public abstract class RecipeViewerSlotWidget<I, W extends RecipeViewerSlotWidget<I, W>> extends Widget<W> implements Interactable {

    @Getter protected RecipeSlotRole recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
    protected EntryList<I> entries;
    @Getter @Setter protected float chance = 1f;
    @Getter @Setter protected UnaryOperator<I> renderMappingFunction = UnaryOperator.identity();

    protected final Class<I> ingredientClass;

    public RecipeViewerSlotWidget(Class<I> ingredientClass) {
       this.ingredientClass = ingredientClass;
    }

    public W recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        if (this.entries != null) rebuildRealSlot();
        return getThis();
    }

    public W value(EntryList<I> entryList) {
        this.entries = entryList;
        if (this.entries != null) rebuildRealSlot();
        if (this.ingredientClass == FluidStack.class) {
            background(GuiTextures.SLOT_FLUID);
        } else {
            background(GuiTextures.SLOT_ITEM); // TODO other types
        }
        return getThis();
    }

    @SuppressWarnings("unchecked")
    public W value(I stack) {
        if (stack.getClass() == ItemStack.class) {
            return value((EntryList<I>) ItemStackList.of((ItemStack) stack));
        } else if (stack.getClass() == FluidStack.class) {
            return value((EntryList<I>) FluidStackList.of((FluidStack) stack));
        } else {
            throw new IllegalArgumentException("Cannot use value(stack) with non-standard stack types! Use value(entryList) instead.");
        }
    }

    protected abstract void rebuildRealSlot();

    public static <I> RecipeViewerSlotWidget<I, ?> create(Class<I> ingredientClass) {
        return RecipeViewerHandler.getCurrent().createRecipeViewerSlot(ingredientClass);
    }

    public static <I> RecipeViewerSlotWidget<I, ?> createFrom(IngredientProvider<I> slot) {
        return RecipeViewerSlotWidget.<I>create(slot.ingredientClass())
                .recipeSlotRole(slot.getRecipeRole())
                .value(slot.getIngredients())
                .chance(slot.chance())
                .renderMappingFunction(slot.renderMappingFunction())
                .copyResizerOf(slot)
                .copyVisualsOf(slot);
    }
}
