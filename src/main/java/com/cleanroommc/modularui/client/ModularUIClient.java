package com.cleanroommc.modularui.client;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIMenuTypes;
import com.cleanroommc.modularui.screen.ContainerScreenWrapper;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ModularUI.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModularUIClient {

    // Event is listened to on the mod event bus
    @SubscribeEvent
    private void registerScreens(FMLClientSetupEvent event) {
        //noinspection deprecation
        event.enqueueWork(() -> MenuScreens.register(ModularUIMenuTypes.MODULAR_CONTAINER.get(), ContainerScreenWrapper::new));
    }
}
