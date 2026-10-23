package brachy.modularui.factory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.jspecify.annotations.Nullable;

public class EntityGuiData extends GuiData {

    private final @Nullable Entity guiHolder;

    public EntityGuiData(Player player, @Nullable Entity guiHolder) {
        super(player);
        this.guiHolder = guiHolder;
    }

    public @Nullable Entity getGuiHolder() {
        return guiHolder;
    }
}
