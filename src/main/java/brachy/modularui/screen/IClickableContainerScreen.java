package brachy.modularui.screen;

import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;

public interface IClickableContainerScreen {

    void modularui$setClickedSlot(@Nullable Slot slot);

    Slot modularui$getClickedSlot();
}
