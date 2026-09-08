package brachy.modularui.integration.recipeviewer.util;

import brachy.modularui.ModularUIConfig;
import brachy.modularui.api.IMuiScreen;
import brachy.modularui.overlay.DebugOverlay;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.ModularScreen;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@UtilityClass
public class RecipeDebugDecoratorUtil {

    public static void addRecipeDebugOverlays(ModularScreen screen) {
        if (!ModularUIConfig.Dev.debugUI()) {
            return;
        }

        MutableObject<ModularScreen> debugOverlay = new MutableObject<>();

        screen.getMainPanel()
                .onOpenAction(panel -> {
                    if (debugOverlay.getValue() != null) {
                        return;
                    }
                    IMuiScreen screenWrapper = panel.getScreen().getScreenWrapper();
                    if (screenWrapper.wrappedScreen() == null) {
                        // safeguard against initing too early
                        return;
                    }

                    ModularScreen overlay = new DebugOverlay(screenWrapper);
                    // set this ASAP to avoid the possibility of double init
                    debugOverlay.setValue(overlay);
                    overlay.constructOverlay(screenWrapper.wrappedScreen());
                    OverlayStack.open(overlay);
                })
                .onCloseAction(panel -> {
                    ModularScreen overlay = debugOverlay.getValue();
                    if (overlay == null) {
                        return;
                    }
                    OverlayStack.close(overlay);
                    debugOverlay.setValue(null);
                });
    }
}
