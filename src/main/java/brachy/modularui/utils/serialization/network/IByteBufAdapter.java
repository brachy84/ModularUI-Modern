package brachy.modularui.utils.serialization.network;

import brachy.modularui.utils.EqualityTest;

import net.minecraft.network.FriendlyByteBuf;

import org.jspecify.annotations.NonNull;

public interface IByteBufAdapter<T> extends IByteBufSerializer<T>, IByteBufDeserializer<T>, EqualityTest<T> {

    @Override
    T deserialize(FriendlyByteBuf buffer);

    @Override
    void serialize(FriendlyByteBuf buffer, T u);

    @Override
    boolean areEqual(@NonNull T t1, @NonNull T t2);
}
