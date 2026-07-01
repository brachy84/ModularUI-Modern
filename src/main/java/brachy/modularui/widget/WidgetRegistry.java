package brachy.modularui.widget;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.serialization.codec.CodecRegistry;
import brachy.modularui.utils.serialization.codec.CodecUtil;

import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public class WidgetRegistry extends CodecRegistry<IWidget, WidgetType<?>> {

    public static final WidgetRegistry INSTANCE = new WidgetRegistry();

    public final MapCodec<IWidget> dispatchCodec = CodecUtil.dispatchNullable(byNameCodec(), IWidget::getType, t -> t.codec().codec());

    private WidgetRegistry() {}

    public <W extends IWidget> WidgetType<W> register(String name, MapCodec<W> codec, Supplier<W> creator) {
        var t = new WidgetType<>(name, codec, creator);
        register(t);
        return t;
    }
}
