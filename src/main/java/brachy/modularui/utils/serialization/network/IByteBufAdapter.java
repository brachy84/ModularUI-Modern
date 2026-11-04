package brachy.modularui.utils.serialization.network;

import brachy.modularui.utils.EqualityTest;

import net.minecraft.network.FriendlyByteBuf;

public interface IByteBufAdapter<T> extends IByteBufSerializer<T>, IByteBufDeserializer<T>, EqualityTest<T> {

    @Override
    T deserialize(FriendlyByteBuf buffer);

    @Override
    void serialize(FriendlyByteBuf buffer, T u);

    @Override
    boolean areEqual(T t1, T t2);
}
