package brachy.modularui.utils.sides;

import brachy.modularui.ModularUI;
import brachy.modularui.utils.RegistryAccessContainer;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

public final class SidedAccessHelper {

    private SidedAccessHelper() {}

    public static RegistryAccess getRegistries() {
        if (ModularUI.isClientThread()) {
            RegistryAccess registries = ClientCallWrapper.getClientRegistries();
            if (registries != null) return registries;
        } else {
            MinecraftServer server = getServer();
            if (server != null) return server.registryAccess();
        }
        return RegistryAccessContainer.current();
    }

    public static RecipeManager getRecipeManager() {
        if (ModularUI.isClientThread()) {
            return ClientCallWrapper.getClientRecipeManager();
        } else {
            return getServer().getRecipeManager();
        }
    }

    public static PotionBrewing getPotionBrewing() {
        if (ModularUI.isClientThread()) {
            return ClientCallWrapper.getClientPotionBrewing();
        } else {
            return getServer().potionBrewing();
        }
    }

    public static RegistryFriendlyByteBuf makeRegistryByteBuf(ByteBuf buffer) {
        return new RegistryFriendlyByteBuf(buffer, getRegistries(), ConnectionType.NEOFORGE);
    }

    private static @Nullable MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
