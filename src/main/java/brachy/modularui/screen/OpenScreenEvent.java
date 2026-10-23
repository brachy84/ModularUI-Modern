package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Event;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenScreenEvent extends Event {

    @Getter private final Screen screen;
    @Getter private final List<ModularScreen> overlays = new ArrayList<>();

    public OpenScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public boolean isModularScreen() {
        return screen instanceof IMuiScreen;
    }

    public @Nullable ModularScreen getModularScreen() {
        return screen instanceof IMuiScreen muiScreen ? muiScreen.screen() : null;
    }

    public void addOverlay(ModularScreen screen) {
        this.overlays.add(screen);
    }
}
