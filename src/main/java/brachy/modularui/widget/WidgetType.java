package brachy.modularui.widget;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.editor.Options;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.serialization.codec.CodecRegistry;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;

import com.mojang.serialization.MapCodec;

import lombok.Getter;

import java.util.function.Supplier;

public final class WidgetType<W extends IWidget> extends CodecRegistry.Entry<W> {

    public static final WidgetType<Widget<?>> WIDGET = reg("widget", Widget.CODEC, Widget::new);
    public static final WidgetType<ParentWidget<?>> PARENT = reg("parent", ParentWidget.CODEC, ParentWidget::new);
    public static final WidgetType<ModularPanel<?>> PANEL = reg("panel", ModularPanel.CODEC, null);
    //public static final WidgetType<Widget<?>> DRAWABLE = reg("drawable", IDrawable.DrawableWidget.CODEC, Widget.CODEC, null);
    public static final WidgetType<TextWidget<?>> TEXT = reg("text", TextWidget.CODEC, () -> new TextWidget<>(Text.str("TEST")));
    public static final WidgetType<ButtonWidget<?>> BUTTON = reg("button", ButtonWidget.CODEC, ButtonWidget::new);

    private static final String WIDGET_TRANSLATION_KEY_FORMAT = "modularui.widget.%s.name";

    public static <W extends IWidget> WidgetType<W> reg(String name, MutableObjectCodec<W> codec, Supplier<W> creator) {
        return reg(name, codec, codec, creator);
    }

    public static <W extends IWidget> WidgetType<W> reg(String name, MapCodec<W> codec, Options<W> options, Supplier<W> creator) {
        return WidgetRegistry.INSTANCE.register(name, codec, options, creator);
    }

    public static void init() {}

    @Getter
    private final Options<W> options;
    private final Supplier<W> creator;

    WidgetType(String name, MapCodec<W> codec, Options<W> options, Supplier<W> creator) {
        super(name, codec);
        this.options = options;
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
