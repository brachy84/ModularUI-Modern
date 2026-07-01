package brachy.modularui.widget;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.serialization.codec.CodecRegistry;
import brachy.modularui.widgets.TextWidget;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public final class WidgetType<W extends IWidget> extends CodecRegistry.Entry<W> {

    public static final WidgetType<Widget<?>> WIDGET = reg("widget", Widget.CODEC, Widget::new);
    public static final WidgetType<ParentWidget<?>> PARENT = reg("parent", ParentWidget.CODEC, ParentWidget::new);
    public static final WidgetType<ModularPanel<?>> PANEL = reg("panel", ModularPanel.CODEC, null);
    public static final WidgetType<IWidget> DRAWABLE = reg("drawable", IDrawable.DrawableWidget.CODEC, null);
    public static final WidgetType<TextWidget<?>> TEXT = reg("text", TextWidget.CODEC, () -> new TextWidget<>(Text.str("TEST")));

    private static final String WIDGET_TRANSLATION_KEY_FORMAT = "modularui.widget.%s.name";

    private static <W extends IWidget> WidgetType<W> reg(String name, MapCodec<W> codec, Supplier<W> creator) {
        return WidgetRegistry.INSTANCE.register(name, codec, creator);
    }

    public static void init() {}

    private final Supplier<W> creator;

    WidgetType(String name, MapCodec<W> codec, Supplier<W> creator) {
        super(name, codec);
        this.creator = creator;
    }

    public String getTranslationKey() {
        return WIDGET_TRANSLATION_KEY_FORMAT.formatted(name().replace(':', '_'));
    }

    public boolean hasCreator() {
        return this.creator != null;
    }

    public W createNewInstance() {
        return this.creator.get();
    }
}
