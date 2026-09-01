package brachy.modularui.factory.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jspecify.annotations.Nullable;

/**
 * A {@link InventoryType} implementation for {@link IItemHandlerModifiable}.
 */
public abstract class ItemHandler extends InventoryType<@Nullable Void> {

    public ItemHandler(String id) {
        super(id);
    }

    public abstract IItemHandlerModifiable getInventory(Player player);

    @Override
    public ItemStack getStackInSlot(Player player, @Nullable Void context, int index) {
        return getInventory(player).getStackInSlot(index);
    }

    @Override
    public void setStackInSlot(Player player, @Nullable Void context, int index, ItemStack stack) {
        getInventory(player).setStackInSlot(index, stack);
    }

    public int getSlotCount(Player player) {
        return getInventory(player).getSlots();
    }

    /**
     * Visits all slots in the inventory with the given visitor function.
     *
     * @param player  player of which inventory to visit
     * @param visitor visit function
     * @return if the visitor function returned true on a slot
     */
    @Override
    public boolean visitAll(Player player, InventoryVisitor<@Nullable Void> visitor) {
        for (int i = 0, n = getSlotCount(player); i < n; ++i) {
            ItemStack stackInSlot = getStackInSlot(player, null, i);
            if (visitor.visit(this, null, i, stackInSlot)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeContext(FriendlyByteBuf byteBuf, @Nullable Void context) {}

    @Override
    public Void readContext(FriendlyByteBuf byteBuf) {
        return null;
    }
}
