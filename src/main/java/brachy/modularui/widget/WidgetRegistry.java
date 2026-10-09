package brachy.modularui.widget;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.editor.Options;
import brachy.modularui.utils.serialization.codec.CodecRegistry;
import brachy.modularui.utils.serialization.codec.CodecUtil;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public class WidgetRegistry extends CodecRegistry<IWidget, WidgetType<?>> {

    public static final WidgetRegistry INSTANCE = new WidgetRegistry();

    public final MapCodec<IWidget> dispatchCodec = CodecUtil.dispatchNullable(byNameCodec(), IWidget::getType, t -> t.codec().codec());

    private WidgetRegistry() {}

    public <W extends IWidget> WidgetType<W> register(String name, MapCodec<W> codec, Options<W> options, Supplier<W> creator) {
        var t = new WidgetType<>(name, codec, options, creator);
        register(t);
        return t;
    }
}
