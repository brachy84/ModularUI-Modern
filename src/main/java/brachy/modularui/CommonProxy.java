package brachy.modularui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.UIFactories;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.network.NetworkHandler;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.test.TestRegistration;
import brachy.modularui.theme.ThemeManager;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.Command;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {

    CommonProxy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onConstruct);
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(this::onRegisterDataReloadListener);
        forgeBus.addListener(this::onRegisterCommand);
        forgeBus.addListener(this::onTick);
        forgeBus.addListener(this::onPlayerLeave);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModularUIConfig.CONFIG, ModularUI.MOD_ID + ".toml");

        /* MUI Initialization */
        UIFactories.init();
        InventoryTypes.init();

        NetworkHandler.init();
        ModularUIMenuTypes.register(modBus);
        if (ModularUI.isDev()) {
            TestRegistration.register(modBus);
        }
    }

    protected void onConstruct(FMLConstructModEvent event) {}

    private void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.containerMenu instanceof ModularContainerMenu containerMenu) {
            containerMenu.onUpdate();
        }
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ModularUI.isClientSide()) {
            ModularNetwork.SERVER.onPlayerLeave(event.getEntity());
        }
    }

    private void onRegisterDataReloadListener(AddReloadListenerEvent event) {
        ModularUI.updateFrozenRegistry(event.getRegistryAccess());
    }

    private void onRegisterCommand(RegisterCommandsEvent event) {
        var command = Commands.literal("mui")
                .then(Commands.literal("reload_themes")
                        .executes(ctx -> {
                            ThemeManager.reload();
                            // TODO translations for this
                            ctx.getSource().sendSuccess(() -> Component.literal("ModularUI Themes reloaded").withStyle(Text.GREEN), true);
                            return Command.SINGLE_SUCCESS;
                        }));
        event.getDispatcher().register(command);
    }
}
