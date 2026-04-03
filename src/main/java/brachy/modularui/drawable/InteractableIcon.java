package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.api.widget.Interactable;

import brachy.modularui.screen.viewport.GuiContext;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true, chain = true)
public class InteractableIcon extends DelegateIcon implements Interactable {

    private IGuiAction.MousePressed mousePressed;
    private IGuiAction.MouseReleased mouseReleased;
    private IGuiAction.MousePressed mouseTapped;
    private IGuiAction.MouseScroll mouseScroll;
    private IGuiAction.KeyPressed keyPressed;
    private IGuiAction.KeyReleased keyReleased;
    private IGuiAction.KeyPressed keyTapped;
    @Setter
    public boolean playClickSound = true;

    @Getter @Setter
    private GuiContext context;

    public InteractableIcon(IIcon icon) {
        super(icon);
    }

    public void playClickSound() {
        if (this.playClickSound) {
            Interactable.playButtonClickSound();
        }
    }

    public GuiContext getContext() {
        return context;
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        if (this.mousePressed != null && this.mousePressed.press(getContext(), button)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseReleased(int button) {
        return this.mouseReleased != null && this.mouseReleased.release(getContext(), button);
    }

    @NotNull
    @Override
    public Result onMouseTapped(int button) {
        if (this.mouseTapped != null && this.mouseTapped.press(getContext(), button)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyPressed != null && this.keyPressed.press(getContext(), modifiers)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return this.keyReleased != null && this.keyReleased.release(getContext(), keyCode, scanCode, modifiers);
    }

    @NotNull
    @Override
    public Result onKeyTapped(int keyCode, int scanCode, int modifiers) {
        if (this.keyTapped != null && this.keyTapped.press(getContext(), modifiers)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double delta) {
        return this.mouseScroll != null && this.mouseScroll.scroll(getContext(), delta);
    }

    public InteractableIcon onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return this;
    }

    public InteractableIcon onMouseReleased(IGuiAction.MouseReleased mouseReleased) {
        this.mouseReleased = mouseReleased;
        return this;
    }

    public InteractableIcon onMouseTapped(IGuiAction.MousePressed mouseTapped) {
        this.mouseTapped = mouseTapped;
        return this;
    }

    public InteractableIcon onMouseScrolled(IGuiAction.MouseScroll mouseScroll) {
        this.mouseScroll = mouseScroll;
        return this;
    }

    public InteractableIcon onKeyPressed(IGuiAction.KeyPressed keyPressed) {
        this.keyPressed = keyPressed;
        return this;
    }

    public InteractableIcon onKeyReleased(IGuiAction.KeyReleased keyReleased) {
        this.keyReleased = keyReleased;
        return this;
    }

    public InteractableIcon onKeyTapped(IGuiAction.KeyPressed keyTapped) {
        this.keyTapped = keyTapped;
        return this;
    }
}
