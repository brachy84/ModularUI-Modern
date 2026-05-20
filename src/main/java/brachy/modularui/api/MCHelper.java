package brachy.modularui.api;

import brachy.modularui.network.ModularNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MCHelper {

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    @SideOnly(Side.CLIENT)
    public static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public static Player getPlayer() {
        return getMc() == null ? null : getMc().player;
    }

    @SideOnly(Side.CLIENT)
    public static boolean closeScreen() {
        getMc().popGuiLayer();
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void popScreen(boolean openParentOnClose, Screen parent) {
        Player player = MCHelper.getPlayer();
        if (player != null) {
            // container should not just be closed here
            // instead they are kept in a stack until all screens are closed
            // prepareCloseContainer(player);
            if (openParentOnClose) {
                Minecraft.getInstance().setScreen(parent);
                ModularNetwork.CLIENT.reopenSyncerOf(parent);
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        } else {
            // we are currently not in a world and want to display the previous screen
            Minecraft.getInstance().setScreen(parent);
        }
    }

    public static void setScreen(Screen screen) {
        if (screen == null) {
            closeScreen();
        } else {
            getMc().setScreen(screen);
        }
    }

    @SideOnly(Side.CLIENT)
    public static Screen getCurrentScreen() {
        return getMc() == null ? null : getMc().screen;
    }

    @SideOnly(Side.CLIENT)
    public static Font getFont() {
        return getMc() == null ? null : getMc().font;
    }

    public static List<Component> getItemToolTip(ItemStack item) {
        return Screen.getTooltipFromItem(getMc(), item);
    }
}
