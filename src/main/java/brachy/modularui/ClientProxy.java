package brachy.modularui;

import brachy.modularui.animation.AnimatorManager;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.DelegateIcon;
import brachy.modularui.drawable.DrawableTooltipComponent;
import brachy.modularui.drawable.GuiSpriteManager;
import brachy.modularui.drawable.HoverableIcon;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.InteractableIcon;
import brachy.modularui.drawable.TextLineTooltipComponent;
import brachy.modularui.drawable.TooltipComponentIcon;
import brachy.modularui.drawable.text.KeyIcon;
import brachy.modularui.drawable.text.TextIcon;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.test.TestHandler;
import brachy.modularui.theme.ThemeManager;
import brachy.modularui.utils.CursorHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import lombok.Getter;

import java.util.function.Function;

public class ClientProxy extends CommonProxy {

    @Getter
    private static final Timer timer60Fps = new Timer(60f, 0);

    ClientProxy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientStartup);
        modBus.addListener(this::onRegisterClientTooltipComponents);
        modBus.addListener(this::onRegisterAssetReloadListeners);
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(this::onUnloadWorld);
        forgeBus.addListener(this::onRegisterAssetReloadListeners);
        if (!ModularUI.isDataGen()) {
            CursorHandler.init();
            AnimatorManager.init();
        }
    }

    @Override
    protected void onPreInit(FMLConstructModEvent event) {
        super.onPreInit(event);
        TestHandler.onPreInit();
    }

    @Override
    protected void onInit(FMLCommonSetupEvent event) {
        super.onInit(event);
        if (!ModularUI.isDataGen()) {
            // enable stencil bits, must call on render thread
            RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
        }
    }

    private void onClientStartup(FMLClientSetupEvent event) {
        //noinspection deprecation,RedundantCast
        event.enqueueWork(() -> MenuScreens.register(ModularUIMenuTypes.MODULAR_CONTAINER.get(),
                (MenuScreens.ScreenConstructor<ModularContainerMenu, ContainerScreenWrapper>) ContainerScreenWrapper::new));
    }

    private void onRegisterClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        Function<IIcon, ClientTooltipComponent> factory = DrawableTooltipComponent::new;
        event.register(Icon.class, factory);
        event.register(DelegateIcon.class, factory);
        event.register(HoverableIcon.class, factory);
        event.register(InteractableIcon.class, factory);
        event.register(KeyIcon.class, factory);
        event.register(TextIcon.class, factory);
        event.register(ClientTooltipComponentIcon.class, ClientTooltipComponentIcon::getClientTooltipComponent);
        event.register(TooltipComponentIcon.class, TooltipComponentIcon::clientComponent);
        event.register(TextLineTooltipComponent.class, c -> c);
    }

    private void onRegisterAssetReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ThemeManager());
        event.registerReloadListener(new GuiSpriteManager(Minecraft.getInstance().textureManager));
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
