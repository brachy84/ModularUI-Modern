package brachy.modularui.core.mixins.client.registries;

import brachy.modularui.utils.RegistryAccessContainer;

import net.minecraft.client.multiplayer.RegistryDataCollector;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryDataCollector.class)
public class RegistryDataCollectorMixin {

    @Inject(method = "collectGameRegistries", at = @At("RETURN"))
    private void modularui$captureClientRegistries(ResourceProvider resourceProvider, RegistryAccess registryAccess,
                                                   boolean isMemoryConnection,
                                                   CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        // don't bother replacing data we already have if we're in singleplayer
        if (isMemoryConnection) return;

        RegistryAccessContainer.update(cir.getReturnValue(), null);
    }
}
