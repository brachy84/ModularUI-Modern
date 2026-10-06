package brachy.modularui.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.mojang.datafixers.util.Either;

import java.util.AbstractList;
import java.util.Optional;

/**
 * A view of {@link TooltipLines} with the element type vanilla/forge expects. Other mods may insert any
 * {@link FormattedText} into this list, which is converted to a {@link Component} before being passed to the
 * underlying {@link TooltipLines}.
 */
public class FormattedTextTooltipLines extends AbstractList<Either<FormattedText, TooltipComponent>> {

    private final TooltipLines delegate;

    public FormattedTextTooltipLines(TooltipLines delegate) {
        this.delegate = delegate;
    }

    @Override
    public Either<FormattedText, TooltipComponent> get(int index) {
        return upcast(this.delegate.get(index));
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public void add(int index, Either<FormattedText, TooltipComponent> element) {
        this.delegate.add(index, toComponent(element));
    }

    @Override
    public Either<FormattedText, TooltipComponent> set(int index, Either<FormattedText, TooltipComponent> element) {
        return upcast(this.delegate.set(index, toComponent(element)));
    }

    @Override
    public Either<FormattedText, TooltipComponent> remove(int index) {
        return upcast(this.delegate.remove(index));
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }

    // Either is immutable, so a Component on the left is always a valid FormattedText
    @SuppressWarnings("unchecked")
    private static Either<FormattedText, TooltipComponent> upcast(Either<Component, TooltipComponent> either) {
        return (Either<FormattedText, TooltipComponent>) (Either<?, ?>) either;
    }

    private static Either<Component, TooltipComponent> toComponent(Either<FormattedText, TooltipComponent> either) {
        return either.mapLeft(FormattedTextTooltipLines::toComponent);
    }

    public static Component toComponent(FormattedText text) {
        if (text instanceof Component component) return component;
        MutableComponent out = Component.empty();
        text.visit((style, str) -> {
            out.append(Component.literal(str).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
