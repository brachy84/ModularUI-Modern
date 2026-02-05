package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Rectangle;

import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps the current gui screen and uses it for overlays.
 */
@ApiStatus.Experimental
public record OverlayScreenWrapper(Screen wrappedScreen, ModularScreen screen) implements IMuiScreen {

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
