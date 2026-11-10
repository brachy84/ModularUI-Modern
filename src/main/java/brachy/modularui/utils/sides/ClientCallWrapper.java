package brachy.modularui.utils.sides;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;

import org.jetbrains.annotations.Nullable;

/**
 * Internal helper class acting as a safeguard for accessing client-only methods
 */
/* package-private */ final class ClientCallWrapper {

    private ClientCallWrapper() {}

    static @Nullable RegistryAccess.Frozen getClientRegistries() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null ? connection.registryAccess() : null;
    }

    static RecipeManager getClientRecipeManager() {
        return Minecraft.getInstance().getConnection().getRecipeManager();
    }

    static PotionBrewing getClientPotionBrewing() {
        return Minecraft.getInstance().getConnection().potionBrewing();
    }
}
