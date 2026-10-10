package brachy.modularui.api.widget;

import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;

public interface IDelegatingWidget extends IWidget, IVanillaSlot {

    IWidget getDelegate();

    @Override
    default @Nullable Slot getVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot ? vanillaSlot.getVanillaSlot() : null;
    }

    @Override
    default boolean handleAsVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot();
    }
}
