package brachy.modularui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.UIFactories;
import brachy.modularui.factory.inventory.InventoryTypes;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {

    public CommonProxy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommand);

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

    @SubscribeEvent
    public void preInit(FMLConstructModEvent event) {}

    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.containerMenu instanceof ModularContainerMenu containerMenu) {
            containerMenu.onUpdate();
        }
    }

    public void registerReloadListeners(AddReloadListenerEvent event) {
        ModularUI.updateFrozenRegistry(event.getRegistryAccess());
        if (ModularUI.isClientThread()) {
            event.addListener(new ThemeManager());
        }
    }

    public void registerCommand(RegisterCommandsEvent event) {
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
