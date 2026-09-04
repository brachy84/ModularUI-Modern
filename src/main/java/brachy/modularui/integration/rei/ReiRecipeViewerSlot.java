package brachy.modularui.integration.rei;

import brachy.modularui.core.mixins.rei.EntryWidgetAccessor;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;

import brachy.modularui.theme.WidgetThemeEntry;

import lombok.Getter;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReiRecipeViewerSlot<I> extends RecipeViewerSlotWidget<I, ReiRecipeViewerSlot<I>> {

    private static final Point ONE = new Point(1, 1);

    @Getter private Slot slotWidget;

    public ReiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);
        this.slotWidget = new EntryWidget(ONE).disableBackground().disableTooltips();

        this.tooltipAutoUpdate(true);
        this.tooltipDynamic(r -> {
            if (slotWidget != null){
                Tooltip tooltip = slotWidget.getCurrentTooltip(TooltipContext.ofMouse());
                if (tooltip == null) return;
                tooltip.entries().forEach(e -> {
                    if (e.isText()){
                        r.addLine(e.getAsText());
                    }
                });
            }
        });
        size(18, 18);
    }

    @Override
    protected void rebuildRealSlot() {
        slotWidget = new EntryWidget(ONE).disableBackground().disableTooltips();
        slotWidget.entries(REIStackConverter.convertToReiEntry(this.entries, this.chance, this.renderMappingFunction));

        if (recipeSlotRole == RecipeSlotRole.INPUT || recipeSlotRole == RecipeSlotRole.CATALYST) {
            slotWidget.markInput();
        } else if (recipeSlotRole == RecipeSlotRole.OUTPUT) {
            slotWidget.markOutput();
        } else {
            slotWidget.unmarkInputOrOutput();
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        if (this.isHovering() && slotWidget instanceof EntryWidgetAccessor accessor){
            accessor.modularui$invokeDrawHighlighted(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        }
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
