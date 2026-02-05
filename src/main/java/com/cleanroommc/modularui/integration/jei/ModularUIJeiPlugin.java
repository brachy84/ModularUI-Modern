package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.integration.jei.handler.JeiContainerHandler;
import com.cleanroommc.modularui.integration.jei.handler.JeiScreenHandler;
import com.cleanroommc.modularui.screen.ContainerScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainerMenu;
import com.cleanroommc.modularui.screen.ScreenWrapper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    @Getter
    private static IJeiRuntime runtime = null;

    @Override
    public ResourceLocation getPluginUid() {
        return ModularUI.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        if (ModularUI.Mods.REI.isLoaded() || ModularUI.Mods.EMI.isLoaded()) return;

        JeiScreenHandler.register(ScreenWrapper.class, registration);
        JeiScreenHandler.register(ContainerScreenWrapper.class, registration);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (ModularUI.Mods.REI.isLoaded() || ModularUI.Mods.EMI.isLoaded()) return;

        JeiContainerHandler.register(ModularContainerMenu.class, registration);
    }
}
