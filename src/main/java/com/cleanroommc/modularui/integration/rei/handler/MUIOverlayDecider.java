package com.cleanroommc.modularui.integration.rei.handler;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;

public class MUIOverlayDecider implements OverlayDecider {

    private final Class<? extends IMuiScreen> clazz;

    public MUIOverlayDecider(Class<? extends IMuiScreen> clazz) {
        this.clazz = clazz;
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
        return this.clazz.isAssignableFrom(screen);
    }

    @Override
    public boolean shouldRecalculateArea(DisplayPanelLocation location, Rectangle rectangle) {
        // thanks for giving me no information about the screen here, REI

        // the screen is Minecraft.getInstance().screen you nerd
        // - screret

        return true;
    }

    @Override
    public <R extends Screen> InteractionResult shouldScreenBeOverlaid(R screen) {
        if (!(screen instanceof IMuiScreen muiScreen)) {
            return InteractionResult.PASS;
        }
        ModularScreen modularScreen = muiScreen.screen();
        return modularScreen.getContext().getRecipeViewerSettings().isEnabled(modularScreen) ? InteractionResult.SUCCESS : InteractionResult.FAIL;

    }

    @Override
    public double getPriority() {
        return 10;
    }
}
