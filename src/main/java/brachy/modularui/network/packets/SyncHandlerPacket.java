package brachy.modularui.network.packets;

import brachy.modularui.api.MCHelper;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.network.NetworkHandler;
import brachy.modularui.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
@AllArgsConstructor
@ApiStatus.Internal
public class SyncHandlerPacket implements NetworkHandler.INetPacket {

    public int networkId;
    public @Nullable String panel;
    public @Nullable String key;
    public boolean action;
    public FriendlyByteBuf packet;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.networkId);
        NetworkUtils.writeStringSafe(buf, this.panel, 256, true);
        NetworkUtils.writeStringSafe(buf, this.key, 256, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    public SyncHandlerPacket(FriendlyByteBuf buf) {
        this.networkId = buf.readVarInt();
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.action = buf.readBoolean();
        this.packet = NetworkUtils.readFriendlyByteBuf(buf);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            if (MCHelper.getPlayer() != null) ModularNetwork.CLIENT.receivePacket(MCHelper.getPlayer(), this);
        } else {
            if (handler.getSender() != null) ModularNetwork.SERVER.receivePacket(handler.getSender(), this);
        }
    }
}
