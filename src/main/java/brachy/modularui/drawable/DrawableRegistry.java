package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.utils.serialization.codec.CodecRegistry;
import brachy.modularui.utils.serialization.codec.CodecUtil;

import com.mojang.serialization.MapCodec;

public class DrawableRegistry extends CodecRegistry<IDrawable, DrawableType<?>> {

    public static final DrawableRegistry INSTANCE = new DrawableRegistry();

    public final MapCodec<IDrawable> dispatchCodec = CodecUtil.dispatchNullable(byNameCodec(), IDrawable::getType, t -> t.codec().codec());

    private DrawableRegistry() {}

    public <D extends IDrawable> DrawableType<D> register(String name, MapCodec<D> codec) {
        var t = new DrawableType<>(name, codec);
        register(t);
        return t;
    }
}
