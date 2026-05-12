package brachy.modularui.drawable.text;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import brachy.modularui.widgets.TextWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class DynamicComponent implements Component, IDrawable {

    private long time = -1;
    private final Supplier<Component> supplier;
    private Style style = Style.EMPTY;
    private Component lastComp;

    public DynamicComponent(Supplier<Component> supplier) {
        this.supplier = supplier;
    }

    private Component getComp() {
        if (supplier == null) {
            return Component.empty();
        }
        if (this.time != ClientScreenHandler.getTicks()) {
            this.lastComp = supplier.get();
            this.time = ClientScreenHandler.getTicks();
            if (this.lastComp instanceof MutableComponent mutableComponent) {
                mutableComponent.setStyle(mutableComponent.getStyle().applyTo(this.style));
            }
        }
        return this.lastComp;
    }

    @Override
    public @NotNull Style getStyle() {
        return getComp().getStyle();
    }

    @Override
    public @NotNull ComponentContents getContents() {
        return getComp().getContents();
    }

    @Override
    public @NotNull List<Component> getSiblings() {
        return getComp().getSiblings();
    }

    @Override
    public @NotNull FormattedCharSequence getVisualOrderText() {
        return getComp().getVisualOrderText();
    }

    @Override
    public TextWidget<?> asWidget() {
        return new TextWidget<>(this::getComp);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        Component comp = getComp();
        if (comp instanceof MutableComponent mutableComponent) {
            Style currentStyle = mutableComponent.getStyle();
            mutableComponent.setStyle(currentStyle.applyTo(this.style));
            if (mutableComponent instanceof ModularComponent modularComponent) {
                modularComponent.draw(context, x, y, width, height, widgetTheme);
            } else {
                FontRenderHelper.drawComponent(comp, context, x, y, width, height, widgetTheme);
            }
            mutableComponent.setStyle(currentStyle);
        } else {
            FontRenderHelper.drawComponent(comp, context, x, y, width, height, widgetTheme);
        }
    }

    public DynamicComponent fallbackStyle(Style style) {
        this.style = style;
        return this;
    }

    public DynamicComponent fallbackStyle(ChatFormatting style) {
        return fallbackStyle(this.style.applyFormat(style));
    }

    public DynamicComponent fallbackStyle(ChatFormatting... style) {
        return fallbackStyle(this.style.applyFormats(style));
    }

    public Style getFallbackStyle() {
        return this.style;
    }
}
