package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.ModularScreen;

/**
 * Gui action listeners that can be registered in {@link ModularScreen#registerGuiActionListener(IGuiAction)}
 */
public interface IGuiAction {

    @FunctionalInterface
    interface MousePressed extends IGuiAction {

        boolean press(double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    interface MouseReleased extends IGuiAction {

        boolean release(double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    interface KeyPressed extends IGuiAction {

        boolean press(int keyCode, int scanCode, int modifiers);
    }

    @FunctionalInterface
    interface KeyReleased extends IGuiAction {

        boolean release(int keyCode, int scanCode, int modifiers);
    }

    @FunctionalInterface
    interface CharTyped extends IGuiAction {

        boolean type(char codePoint, int modifiers);
    }

    @FunctionalInterface
    interface MouseScroll extends IGuiAction {

        boolean scroll(double mouseX, double mouseY, double delta);
    }

    @FunctionalInterface
    interface MouseDrag extends IGuiAction {

        boolean drag(double mouseX, double mouseY, int button, double dragX, double dragY);
    }
}
