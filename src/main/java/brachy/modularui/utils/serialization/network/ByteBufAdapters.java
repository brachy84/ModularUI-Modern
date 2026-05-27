package brachy.modularui.utils.serialization.network;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.NetworkUtils;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public class ByteBufAdapters {

    public static final IByteBufAdapter<ItemStack> ITEM_STACK = makeAdapter(FriendlyByteBuf::readItem, FriendlyByteBuf::writeItem, ItemStack::matches);
    public static final IByteBufAdapter<FluidStack> FLUID_STACK = makeMemberAdapter(FluidStack::readFromPacket, FluidStack::writeToPacket, FluidStack::isFluidStackIdentical);
    public static final IByteBufAdapter<CompoundTag> NBT = makeAdapter(FriendlyByteBuf::readNbt, FriendlyByteBuf::writeNbt, null);
    public static final IByteBufAdapter<String> STRING = makeAdapter(NetworkUtils::readStringSafe, NetworkUtils::writeStringSafe, null);
    public static final IByteBufAdapter<ByteBuf> BYTE_BUF = makeAdapter(NetworkUtils::readByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<FriendlyByteBuf> FRIENDLY_BYTE_BUF = makeAdapter(NetworkUtils::readFriendlyByteBuf, NetworkUtils::writeByteBuf, null);

    public static final IByteBufAdapter<Integer> INT = makeAdapter(FriendlyByteBuf::readInt, FriendlyByteBuf::writeInt, null);
    public static final IByteBufAdapter<Long> LONG = makeAdapter(FriendlyByteBuf::readLong, FriendlyByteBuf::writeLong, null);
    public static final IByteBufAdapter<Float> FLOAT = makeAdapter(FriendlyByteBuf::readFloat, FriendlyByteBuf::writeFloat, null);
    public static final IByteBufAdapter<Double> DOUBLE = makeAdapter(FriendlyByteBuf::readDouble, FriendlyByteBuf::writeDouble, null);
    public static final IByteBufAdapter<Boolean> BOOL = makeAdapter(FriendlyByteBuf::readBoolean, FriendlyByteBuf::writeBoolean, null);
    public static final IByteBufAdapter<Byte> BYTE = makeAdapter(FriendlyByteBuf::readByte, (buffer, b) -> buffer.writeByte(b), null);
    public static final IByteBufAdapter<Short> SHORT = makeAdapter(FriendlyByteBuf::readShort, (buffer, b) -> buffer.writeShort(b), null);
    public static final IByteBufAdapter<Character> CHAR = makeAdapter(FriendlyByteBuf::readChar, (buffer, b) -> buffer.writeChar(b), null);

    public static final IByteBufAdapter<BlockState> BLOCKSTATE = makeAdapterFromCodec(BlockState.CODEC, BlockState::equals);
    public static final IByteBufAdapter<BlockPos> BLOCKPOS = makeAdapterFromCodec(BlockPos.CODEC, BlockPos::equals);
    public static final IByteBufAdapter<GlobalPos> GLOBAL_POS = makeAdapterFromCodec(GlobalPos.CODEC, GlobalPos::equals);
    public static final IByteBufAdapter<ResourceLocation> RESOURCE_LOCATION = makeAdapterFromCodec(ResourceLocation.CODEC, ResourceLocation::equals);
    public static final IByteBufAdapter<UUID> UUID = makeAdapter(FriendlyByteBuf::readUUID, FriendlyByteBuf::writeUUID, java.util.UUID::equals);
    public static final IByteBufAdapter<Component> COMPONENT = makeAdapter(FriendlyByteBuf::readComponent, FriendlyByteBuf::writeComponent,
            (a, b) -> Objects.equals(a.getString(), b.getString()));

    public static final IByteBufAdapter<byte[]> BYTE_ARR = new IByteBufAdapter<>() {

        @Override
        public byte[] deserialize(FriendlyByteBuf buffer) {
            return buffer.readByteArray();
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, byte[] u) {
            buffer.writeByteArray(u);
        }

        @Override
        public boolean areEqual(byte @NotNull [] t1, byte @NotNull [] t2) {
            if (t1.length != t2.length) return false;
            for (int i = 0; i < t1.length; i++) {
                if (t1[i] != t2[i]) return false;
            }
            return true;
        }
    };

    public static final IByteBufAdapter<long[]> LONG_ARR = new IByteBufAdapter<>() {

        @Override
        public long[] deserialize(FriendlyByteBuf buffer) {
            return buffer.readLongArray();
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, long[] u) {
            buffer.writeLongArray(u);
        }

        @Override
        public boolean areEqual(long @NotNull [] t1, long @NotNull [] t2) {
            if (t1.length != t2.length) return false;
            for (int i = 0; i < t1.length; i++) {
                if (t1[i] != t2[i]) return false;
            }
            return true;
        }
    };

    public static final IByteBufAdapter<BigInteger> BIG_INT = new IByteBufAdapter<>() {

        @Override
        public BigInteger deserialize(FriendlyByteBuf buffer) {
            return new BigInteger(buffer.readByteArray());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, BigInteger u) {
            buffer.writeByteArray(u.toByteArray());
        }

        @Override
        public boolean areEqual(@NotNull BigInteger t1, @NotNull BigInteger t2) {
            return t1.equals(t2);
        }
    };

    public static final IByteBufAdapter<BigDecimal> BIG_DECIMAL = new IByteBufAdapter<>() {

        @Override
        public BigDecimal deserialize(FriendlyByteBuf buffer) {
            return new BigDecimal(BIG_INT.deserialize(buffer), buffer.readVarInt());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, BigDecimal u) {
            BIG_INT.serialize(buffer, u.unscaledValue());
            buffer.writeVarInt(u.scale());
        }

        @Override
        public boolean areEqual(@NotNull BigDecimal t1, @NotNull BigDecimal t2) {
            return t1.equals(t2);
        }
    };

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer,
                                                     @Nullable EqualityTest<T> tester) {
        return new IByteBufAdapter<>() {

            @Override
            public T deserialize(FriendlyByteBuf buffer) {
                return deserializer.deserialize(buffer);
            }

            @Override
            public void serialize(FriendlyByteBuf buffer, T u) {
                serializer.serialize(buffer, u);
            }

            @Override
            public boolean areEqual(@NotNull T t1, @NotNull T t2) {
                return tester != null ? tester.areEqual(t1, t2) : Objects.equals(t1, t2);
            }
        };
    }

    public static <T> IByteBufAdapter<T> makeMemberAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                           @NotNull IByteBufMemberSerializer<T> memberSerializer,
                                                           @Nullable EqualityTest<T> comparator) {
        return makeAdapter(deserializer, memberSerializer.asBasic(), comparator);
    }
    public static <T> IByteBufAdapter<T> makeAdapterFromCodec(@NotNull Codec<T> codec, @NotNull EqualityTest<T> equals) {
        return new IByteBufAdapter<>() {

            @Override
            public T deserialize(FriendlyByteBuf buffer) {
                return buffer.readJsonWithCodec(codec);
            }

            @Override
            public void serialize(FriendlyByteBuf buffer, T u) {
                buffer.writeJsonWithCodec(codec, u);
            }

            @Override
            public boolean areEqual(@NotNull T t1, @NotNull T t2) {
                return equals.areEqual(t1, t2);
            }
        };
    }
}
