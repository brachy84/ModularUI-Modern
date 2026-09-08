package brachy.modularui.integration.rei;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.rei.handler.REIScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;

import brachy.modularui.test.TestRecipeViewerGuis;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class ModularUIReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        REIScreenHandler.register(ScreenWrapper.class, zones);
        REIScreenHandler.register(ContainerScreenWrapper.class, zones);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        REIScreenHandler.register(ScreenWrapper.class, registry);
        REIScreenHandler.register(ContainerScreenWrapper.class, registry);
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        if (ModularUI.isDev()) {
            TestRecipeViewerGuis.REI.registerCategory(registry);
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        if (ModularUI.isDev()) {
            TestRecipeViewerGuis.REI.registerDisplay(registry);
        }
    }
}
