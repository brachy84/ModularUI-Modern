package brachy.modularui.api.widget;

import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;

/**
 * Marks a {@link IWidget} as containing a vanilla item slot.
 */
public interface IVanillaSlot {

    /**
     * @return the item slot of this widget
     */
    @Nullable Slot getVanillaSlot();

    boolean handleAsVanillaSlot();
}
