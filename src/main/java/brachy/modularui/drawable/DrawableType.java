package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.utils.serialization.codec.CodecRegistry;

import com.mojang.serialization.MapCodec;

public class DrawableType<D extends IDrawable> extends CodecRegistry.Entry<D> {

    public static final DrawableType<Rectangle> RECTANGLE = reg("rectangle", Rectangle.CODEC);
    public static final DrawableType<Circle> CIRCLE = reg("circle", Circle.CODEC);
    public static final DrawableType<Icon> ICON = reg("icon", Icon.CODEC);
    public static final DrawableType<RichText> RICH_TEXT = reg("rich_text", RichText.CODEC);
    public static final DrawableType<ModularComponent> TEXT = reg("text", ModularComponent.CODEC);
    public static final DrawableType<UITexture> TEXTURE = reg("texture", UITexture.CODEC);
    public static final DrawableType<SubAreaDrawable> SUB_AREA = reg("sub_area", SubAreaDrawable.CODEC);
    public static final DrawableType<Scrollbar> SCROLLBAR = reg("scrollbar", Scrollbar.CODEC);
    public static final DrawableType<FluidDrawable> FLUID = reg("fluid", FluidDrawable.CODEC);
    public static final DrawableType<ItemDrawable> ITEM = reg("item", ItemDrawable.CODEC);

    private static final String DRAWABLE_TRANSLATION_KEY_FORMAT = "modularui.drawable.%s.name";

    public static <D extends IDrawable> DrawableType<D> reg(String name, MapCodec<D> codec) {
        return DrawableRegistry.INSTANCE.register(name, codec);
    }

    public static void init() {}

    DrawableType(String name, MapCodec<D> codec) {
        super(name, codec);
    }

    public String getTranslationKey() {
        return DRAWABLE_TRANSLATION_KEY_FORMAT.formatted(name().replace(':', '_'));
    }
}
