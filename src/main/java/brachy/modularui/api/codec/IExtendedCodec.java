package brachy.modularui.api.codec;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.serialization.codec.MutableMapCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface IExtendedCodec<A> extends EqualityTest<A> {

    @SuppressWarnings("unchecked")
    static <A> @Nullable IExtendedCodec<A> getFrom(Codec<A> codec) {
        if (codec instanceof IExtendedCodec<?> extendedCodec) return (IExtendedCodec<A>) extendedCodec;
        if (codec instanceof MapCodec.MapCodecCodec<A> mcc && mcc.codec() instanceof IExtendedCodec<?> mmc) {
            return (IExtendedCodec<A>) mmc;
        }
        if (codec instanceof MutableMapCodec.MutableCodecCodec<A> mcc && mcc.codec() instanceof IExtendedCodec<?> extendedCodec) {
            return (IExtendedCodec<A>) extendedCodec;
        }
        return null;
    }

    A copy(A instance);

    <B extends A> B copyFields(A from, B to);

    default String convertToString(A a, boolean pretty) {
        return convertToString(a, pretty ? 0 : -1);
    }

    String convertToString(A a, int indent);

    @Override
    default boolean areEqual(@NotNull A t1, @NotNull A t2) {
        return Objects.equals(t1, t2);
    }
}
