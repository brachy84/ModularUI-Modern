package brachy.modularui.core.mixins.client;

import brachy.modularui.ClientProxy;
import brachy.modularui.screen.ClientScreenHandler;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public Screen screen;

    @Inject(method = "runTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I", shift = At.Shift.AFTER))
    public void modularui$updateTimer(CallbackInfo ci) {
        int ticks = ClientProxy.getTimer60Fps().advanceTime(Util.getMillis());
        for (int j = 0; j < Math.min(20, ticks); ++j) {
            ClientScreenHandler.onFrameUpdate();
        }
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    public void modularui$trackRealScreenClose(Screen guiScreen, CallbackInfo ci) {
        if (guiScreen == null) {
            // the ScreenEvent.Closing is also closed when the screen is transitioning to another screen,
            // but we only want to know when the next screen is null, so that all screens close.
            ClientScreenHandler.onCloseScreens(this.screen);
        }
    }
}
