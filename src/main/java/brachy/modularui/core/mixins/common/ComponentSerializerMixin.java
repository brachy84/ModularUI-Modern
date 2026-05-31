package brachy.modularui.core.mixins.common;

import brachy.modularui.ModularUI;
import brachy.modularui.drawable.text.ModularComponent;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Type;

/**
 * We need this since nested components are potentially modular too
 */
@Mixin(Component.Serializer.class)
public class ComponentSerializerMixin {

    @WrapOperation(
            method = "serialize(Lnet/minecraft/network/chat/Component;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component$Serializer;serialize(Lnet/minecraft/network/chat/Component;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;"))
    public JsonElement serialize(Component.Serializer instance, Component src, Type typeOfSrc, JsonSerializationContext context, Operation<JsonElement> original) {
        if (src instanceof ModularComponent mc) {
            var d = ModularComponent.CODEC.mutableCodec().encodeJson(mc);
            var res = d.result();
            if (res.isPresent()) return res.get();
            ModularUI.LOGGER.error("Error encoding nested ModularComponent: {}", d.error().orElseThrow().message());
        }
        return original.call(instance, src, typeOfSrc, context);
    }

    @WrapOperation(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/MutableComponent;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component$Serializer;deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/MutableComponent;"))
    public MutableComponent deserialize(Component.Serializer instance, JsonElement json, Type typeOfT, JsonDeserializationContext context, Operation<MutableComponent> original) {
        MutableComponent comp = original.call(instance, json, typeOfT, context);
        if (comp.getClass() == MutableComponent.class && json instanceof JsonObject jsonObj && ModularComponent.CODEC.hasAnyField(jsonObj)) {
            ModularComponent mc = comp.asModular();
            var d = ModularComponent.CODEC.mutableCodec().parseJson(jsonObj, mc);
            var res = d.result();
            if (res.isEmpty()) throw new JsonParseException(d.error().orElseThrow().message());
            return res.get();
        }
        return comp;
    }
}
