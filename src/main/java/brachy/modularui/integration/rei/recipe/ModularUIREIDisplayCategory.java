package brachy.modularui.integration.rei.recipe;

import brachy.modularui.api.widget.IWidget;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;

import java.util.List;

public abstract class ModularUIREIDisplayCategory<W extends IWidget, D extends ModularUIREIDisplay<W>>
        implements DisplayCategory<D> {

    @Override
    public List<Widget> setupDisplay(D display, Rectangle bounds) {
        return display.createWidgets(bounds);
    }
}
