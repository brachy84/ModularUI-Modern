package brachy.modularui.integration.jei;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import com.mojang.blaze3d.platform.Window;

import lombok.Getter;
import mezz.jei.api.gui.handlers.IGuiProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * This needs to be an immutable class, otherwise JEI shits itself.
 */
public class ModularUIJeiProperties implements IGuiProperties {

    private static final int DEFAULT_GUI_WIDTH = 176, DEFAULT_GUI_HEIGHT = 166;

    @Getter
    private final Class<? extends Screen> screenClass;
    @Getter
    private final int guiLeft;
    @Getter
    private final int guiTop;
    @Getter
    private final int guiXSize;
    @Getter
    private final int guiYSize;
    @Getter
    private final int screenWidth;
    @Getter
    private final int screenHeight;

    public ModularUIJeiProperties(IMuiScreen screen) {
        this.screenClass = screen.wrappedScreen().getClass();

        // we have to assign default values to gui size if the size is 0 (e.g. during the instant it's created)
        //  because MUI initializes the screen/panel sizing data after JEI wants to use them
        Window window = Minecraft.getInstance().getWindow();
        Area screenArea = screen.screen().getScreenArea();
        this.screenWidth = screenArea.width == 0 ? window.getGuiScaledWidth() : screenArea.width;
        this.screenHeight = screenArea.height == 0 ? window.getGuiScaledHeight() : screenArea.height;

        Area mainArea = screen.screen().getMainPanel().getArea();
        this.guiXSize = mainArea.width == 0 ? DEFAULT_GUI_WIDTH : mainArea.width;
        this.guiYSize = mainArea.height == 0 ? DEFAULT_GUI_HEIGHT : mainArea.height;
        // don't check mainArea x/y here because those can actually be 0
        this.guiLeft = mainArea.width == 0 ? (this.screenWidth - DEFAULT_GUI_WIDTH) / 2 : mainArea.x;
        this.guiTop = mainArea.height == 0 ? (this.screenHeight - DEFAULT_GUI_HEIGHT) / 2 : mainArea.y;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("x", getGuiLeft())
                .append("y", getGuiTop())
                .append("width", getGuiXSize())
                .append("height", getGuiYSize())
                .append("screenWidth", getScreenWidth())
                .append("screenHeight", getScreenHeight())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModularUIJeiProperties that = (ModularUIJeiProperties) o;
        return this.guiLeft == that.guiLeft && this.guiTop == that.guiTop &&
                this.guiXSize == that.guiXSize && this.guiYSize == that.guiYSize &&
                this.screenWidth == that.screenWidth && this.screenHeight == that.screenHeight &&
                Objects.equals(this.screenClass, that.screenClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screenClass, guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight);
    }
}
