package brachy.modularui.integration.emi.recipe;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;

public class SlotWidgetWrapper<W extends SlotWidgetWrapper<W>> extends Widget<W> implements Interactable {

    private final SlotWidget slotWidget;
    private final int x, y;

    public SlotWidgetWrapper(SlotWidget slotWidget) {
        this.slotWidget = slotWidget;
        Bounds bounds = this.slotWidget.getBounds();
        size(bounds.width(), bounds.height());
        this.x = bounds.x();
        this.y = bounds.y();

        tooltipAutoUpdate(true);
        tooltipDynamic(tooltip -> {
            for (ClientTooltipComponent ctc : this.slotWidget.getTooltip(getContext().getAbsMouseX(), getContext().getAbsMouseY())) {
                tooltip.addDrawableLine(new ClientTooltipComponentIcon(ctc));
            }
        });
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        tooltip().clearText();
        for (ClientTooltipComponent ctc : this.slotWidget.getTooltip(getContext().getAbsMouseX(), getContext().getAbsMouseY())) {
            tooltip().addDrawableLine(new ClientTooltipComponentIcon(ctc));
        }
        super.draw(context, widgetTheme);
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
