package brachy.modularui.core.mixins.common;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CombinedInvWrapper.class)
public interface CombinedInvWrapperAccessor {

    @Accessor
    IItemHandlerModifiable[] getItemHandler();
}
