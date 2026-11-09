package brachy.modularui;

import brachy.modularui.animation.AnimatorManager;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.DelegateIcon;
import brachy.modularui.drawable.DrawableTooltipComponent;
import brachy.modularui.drawable.GuiSpriteManager;
import brachy.modularui.drawable.HoverableIcon;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.InteractableIcon;
import brachy.modularui.drawable.TooltipComponentIcon;
import brachy.modularui.drawable.text.KeyIcon;
import brachy.modularui.drawable.text.TextIcon;
import brachy.modularui.editor.EditorScreen;
import brachy.modularui.factory.ClientGUI;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.screen.BuildPanelEvent;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.test.TestHandler;
import brachy.modularui.theme.ThemeManager;
import brachy.modularui.utils.CursorHandler;
import brachy.modularui.widget.WidgetSerializer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.Command;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
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
        forgeBus.addListener(this::onBuildPanel);
        forgeBus.addListener(this::onRegisterAssetReloadListeners);
        forgeBus.addListener(this::onRegisterCommand);
        forgeBus.addListener(this::onUnloadWorld);
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

    private void onBuildPanel(BuildPanelEvent event) {
        WidgetSerializer.applyModifications(event.getId(), event.getOpeningPanel());
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
    }

    private void onRegisterAssetReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ThemeManager());
        event.registerReloadListener(new GuiSpriteManager(Minecraft.getInstance().textureManager));
    }

    private void onRegisterCommand(RegisterClientCommandsEvent event) {
        var command = Commands.literal("mui")
                .then(Commands.literal("reload_themes")
                        .executes(ctx -> {
                            ThemeManager.reload();
                            // TODO translations for this
                            ctx.getSource().sendSuccess(() -> Component.literal("ModularUI Themes reloaded").withStyle(Text.GREEN), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("editor")
                        .executes(ctx -> {
                            ClientGUI.open(new EditorScreen(ModularUI.MOD_ID));
                            return Command.SINGLE_SUCCESS;
                        }));
        event.getDispatcher().register(command);
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
