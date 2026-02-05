package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.network.MUINetwork;
import com.cleanroommc.modularui.network.ModularNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CloseAllGuiPacket implements MUINetwork.INetPacket {

    public CloseAllGuiPacket(FriendlyByteBuf buffer) {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularNetwork.CLIENT.closeAll(MCHelper.getPlayer(), false);
        } else {
            ModularNetwork.SERVER.closeAll(handler.getSender(), false);
        }
    }
}
