package brachy.modularui.core.mixins.common;

import brachy.modularui.drawable.text.ModularAwareComponentCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes the vanilla recursive component codec aware of {@link brachy.modularui.drawable.text.ModularComponent}, so that
 * nested modular components keep their extra properties when (de)serialized through vanilla (tooltips, networking, ...).
 * <p>
 * The codec returned by {@code createCodec} is the one the recursive {@link ComponentSerialization#CODEC} delegates to,
 * so wrapping it here applies the modular handling at every node of the tree.
 */
@Mixin(ComponentSerialization.class)
public class ComponentSerializationMixin {

    @ModifyReturnValue(method = "createCodec", at = @At("RETURN"))
    private static Codec<Component> modularui$wrapModular(Codec<Component> original) {
        return new ModularAwareComponentCodec(original);
    }
}
