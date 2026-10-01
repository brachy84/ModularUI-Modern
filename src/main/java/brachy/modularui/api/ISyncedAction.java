package brachy.modularui.api;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface ISyncedAction {

    @ApiStatus.OverrideOnly
    void invoke(FriendlyByteBuf packet);
}
