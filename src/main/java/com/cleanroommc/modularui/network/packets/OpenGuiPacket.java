package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.utils.NetworkUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class OpenGuiPacket<T extends GuiData> implements NetworkHandler.INetPacket {

    private int windowId;
    private int networkId;
    private UIFactory<T> factory;
    private FriendlyByteBuf data;

    public OpenGuiPacket(FriendlyByteBuf buf) {
        this.windowId = buf.readVarInt();
        this.networkId = buf.readVarInt();
        this.factory = (UIFactory<T>) GuiManager.getFactory(buf.readResourceLocation());
        this.data = NetworkUtils.readFriendlyByteBuf(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.windowId);
        buf.writeVarInt(this.networkId);
        buf.writeResourceLocation(this.factory.getFactoryName());
        NetworkUtils.writeByteBuf(buf, this.data);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            GuiManager.openFromClient(this.windowId, this.networkId, this.factory, this.data,
                    Minecraft.getInstance().player);
        } else if (handler.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            T guiData = this.factory.readGuiData(handler.getSender(), this.data);
            GuiManager.open(this.factory, guiData, handler.getSender());
        }
    }
}
