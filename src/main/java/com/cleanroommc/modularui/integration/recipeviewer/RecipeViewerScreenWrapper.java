package com.cleanroommc.modularui.integration.recipeviewer;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class RecipeViewerScreenWrapper implements IMuiScreen {

    @Getter
    private final ModularScreen screen;

    public RecipeViewerScreenWrapper(ModularScreen screen) {
        this.screen = screen;
    }

    @Override
    public Screen wrappedScreen() {
        return Minecraft.getInstance().screen;
    }

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
