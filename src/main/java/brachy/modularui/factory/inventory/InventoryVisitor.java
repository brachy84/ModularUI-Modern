package brachy.modularui.factory.inventory;

import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

/**
 * A function to visit a slot in a player bound inventory.
 */
public interface InventoryVisitor<T extends @Nullable Object> {

    /**
     * Called on visiting a slot in a player bound inventory.
     *
     * @param type        type of the current inventory
     * @param context     additional context of the slot location (usually null, but is a string for curios)
     * @param index       index of the slot
     * @param stackInSlot content of the slot
     * @return true if no further slots should be visited
     */
    boolean visit(InventoryType<T> type, T context, int index, ItemStack stackInSlot);
}
