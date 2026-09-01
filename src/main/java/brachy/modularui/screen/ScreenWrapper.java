package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;

@Accessors(fluent = true)
@OnlyIn(Dist.CLIENT)
public class ScreenWrapper extends Screen implements IMuiScreen {

    @Getter
    private final @NonNull ModularScreen screen;

    public ScreenWrapper(@NonNull ModularScreen screen) {
        super(Component.empty());
        this.screen = screen;
        this.screen.construct(this);
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics) {
        handleDrawBackground(guiGraphics, super::renderBackground);
    }

    @Override
    public boolean isPauseScreen() {
        return this.screen.isPauseScreen();
    }

    @Override
    public String toString() {
        return "Wrapper(" + screen() + ")";
    }
}
