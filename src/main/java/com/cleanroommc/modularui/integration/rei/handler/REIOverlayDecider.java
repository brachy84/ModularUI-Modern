package com.cleanroommc.modularui.integration.rei.handler;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;

public class REIOverlayDecider implements OverlayDecider {

    @Override
    public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
        return IMuiScreen.class.isAssignableFrom(screen);
    }

    @Override
    public boolean shouldRecalculateArea(DisplayPanelLocation location, Rectangle rectangle) {
        // thanks for giving me no information about the screen here, REI
        return true;
    }

    @Override
    public <R extends Screen> InteractionResult shouldScreenBeOverlaid(R screen) {
        ModularScreen modularScreen = ((IMuiScreen) screen).screen();
        return modularScreen.getContext().getXeiSettings().isEnabled(modularScreen) ? InteractionResult.SUCCESS : InteractionResult.FAIL;

    }

    @Override
    public double getPriority() {
        return 10;
    }
}
