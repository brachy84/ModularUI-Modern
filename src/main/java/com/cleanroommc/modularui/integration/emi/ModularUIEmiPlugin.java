package com.cleanroommc.modularui.integration.emi;

import com.cleanroommc.modularui.integration.emi.handler.EmiScreenHandler;
import com.cleanroommc.modularui.screen.ContainerScreenWrapper;
import com.cleanroommc.modularui.screen.ScreenWrapper;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class ModularUIEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        EmiScreenHandler.register(ScreenWrapper.class, registry);
        EmiScreenHandler.register(ContainerScreenWrapper.class, registry);
    }
}
