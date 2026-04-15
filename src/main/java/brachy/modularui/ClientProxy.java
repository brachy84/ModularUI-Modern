package brachy.modularui;

import brachy.modularui.animation.AnimatorManager;
import brachy.modularui.client.CursorHandler;
import brachy.modularui.drawable.DrawableSerialization;

import brachy.modularui.network.ModularNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;

import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public class ClientProxy extends CommonProxy {

    @Getter
    private static final Timer timer60Fps = new Timer(60f, 0);

    public ClientProxy() {
        MinecraftForge.EVENT_BUS.addListener(this::onUnloadWorld);
        if (!ModularUI.isDataGen()) {
            CursorHandler.init();
            AnimatorManager.init();
            DrawableSerialization.init();
        }
    }

    @Override
    public void preInit(FMLConstructModEvent event) {
        super.preInit(event);
        if (!ModularUI.isDataGen()) {
            // enable stencil bits, must call on render thread
            RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
        }
    }

    private void onUnloadWorld(LevelEvent.Unload event) {
        if (Minecraft.getInstance().player != null) {
            ModularNetwork.CLIENT.onPlayerLeave(Minecraft.getInstance().player);

            if (Minecraft.getInstance().hasSingleplayerServer()) {
                // we need to handle single player here, since PlayerLoggedOutEvent is not triggered for some reason
                ModularNetwork.SERVER.onPlayerLeave(Minecraft.getInstance().player);
            }
        }
    }
}
