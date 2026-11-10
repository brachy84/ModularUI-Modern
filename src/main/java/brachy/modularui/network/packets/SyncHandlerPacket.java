package brachy.modularui.network.packets;

import brachy.modularui.ModularUI;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.utils.NetworkUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncHandlerPacket(int networkId, String panel, String key, boolean action, RegistryFriendlyByteBuf packet)
        implements CustomPacketPayload {

    public static final ResourceLocation ID = ModularUI.id("sync_message");
    public static final Type<SyncHandlerPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHandlerPacket> CODEC = StreamCodec
            .ofMember(SyncHandlerPacket::encode, SyncHandlerPacket::decode);

    public static SyncHandlerPacket decode(RegistryFriendlyByteBuf buf) {
        int networkId = buf.readVarInt();
        String panel = NetworkUtils.readStringSafe(buf);
        String key = NetworkUtils.readStringSafe(buf);
        boolean action = buf.readBoolean();
        RegistryFriendlyByteBuf packet = buf.mui$wrapByteBuf(NetworkUtils.readByteBuf(buf));

        return new SyncHandlerPacket(networkId, panel, key, action, packet);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.networkId);
        NetworkUtils.writeStringSafe(buf, this.panel);
        NetworkUtils.writeStringSafe(buf, this.key, 64, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    public void execute(IPayloadContext context) {
        ModularNetwork.get(context.flow().isClientbound()).receivePacket(context.player(), this);
    }

    @Override
    public Type<SyncHandlerPacket> type() {
        return TYPE;
    }
}
