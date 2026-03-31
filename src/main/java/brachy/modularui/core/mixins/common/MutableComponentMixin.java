package brachy.modularui.core.mixins.common;

import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.drawable.text.ToModularComponent;

import net.minecraft.network.chat.MutableComponent;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin implements ToModularComponent {

    @Override
    public ModularComponent asModular() {
        MutableComponent self = (MutableComponent) (Object) this;
        return new ModularComponent(self.getContents(), self.getSiblings(), self.getStyle());
    }
}
