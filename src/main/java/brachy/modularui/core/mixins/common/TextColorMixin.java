package brachy.modularui.core.mixins.common;

import net.minecraft.network.chat.TextColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextColor.class)
public class TextColorMixin {

    /**
     * @reason Minecraft uses Integer.parseInt which fails when alpha is specified because of signed vs. unsigned
     */
    @Inject(method = "parseColor", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;parseInt(Ljava/lang/String;I)I"), cancellable = true)
    private static void fixDecode(String hexString, CallbackInfoReturnable<TextColor> cir) {
        try {
            cir.setReturnValue(TextColor.fromRgb((int) (long) Long.decode(hexString)));
        } catch (NumberFormatException e) {
            cir.setReturnValue(null);
        }
    }
}
