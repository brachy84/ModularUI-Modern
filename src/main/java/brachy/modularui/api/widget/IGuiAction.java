package brachy.modularui.api.widget;

import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.viewport.GuiContext;

/**
 * Gui action listeners that can be registered in {@link ModularScreen#registerGuiActionListener(IGuiAction)}
 */
public interface IGuiAction {

    @FunctionalInterface
    interface MousePressed extends IGuiAction {

        boolean press(GuiContext context, int button);
    }

    @FunctionalInterface
    interface MouseReleased extends IGuiAction {

        boolean release(GuiContext context, int button);
    }

    @FunctionalInterface
    interface KeyPressed extends IGuiAction {

        boolean press(GuiContext context, int modifiers);
    }

    @FunctionalInterface
    interface KeyReleased extends IGuiAction {

        boolean release(GuiContext context, int keyCode, int scanCode, int modifiers);
    }

    @FunctionalInterface
    interface CharTyped extends IGuiAction {

        boolean type(GuiContext context, char codePoint, int modifiers);
    }

    @FunctionalInterface
    interface MouseScroll extends IGuiAction {

        boolean scroll(GuiContext context, double delta);
    }

    @FunctionalInterface
    interface MouseDrag extends IGuiAction {

        boolean drag(GuiContext context, int button, double dragX, double dragY);
    }

    @FunctionalInterface
    interface MouseEnterArea extends IGuiAction {

        void enter(GuiContext context);
    }

    @FunctionalInterface
    interface MouseLeaveArea extends IGuiAction {

        void leave(GuiContext context, int timeBelowMouse);
    }

    @FunctionalInterface
    interface MouseStartHover extends IGuiAction {

        void startHover(GuiContext context);
    }

    @FunctionalInterface
    interface MouseEndHover extends IGuiAction {

        void endHover(GuiContext context, int timeHovered);
    }
}
