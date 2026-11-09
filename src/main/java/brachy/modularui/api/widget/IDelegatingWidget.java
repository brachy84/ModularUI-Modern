package brachy.modularui.api.widget;

import net.minecraft.world.inventory.Slot;

public interface IDelegatingWidget extends IWidget, IVanillaSlot {

    static boolean isDelegating(IWidget widget) {
        return isDelegating(widget.getParent(), widget);
    }

    static boolean isDelegating(IWidget parent, IWidget widget) {
        return parent instanceof IDelegatingWidget d && d.getDelegate() == widget;
    }

    IWidget getDelegate();

    @Override
    default Slot getVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot ? vanillaSlot.getVanillaSlot() : null;
    }

    @Override
    default boolean handleAsVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot();
    }
}
