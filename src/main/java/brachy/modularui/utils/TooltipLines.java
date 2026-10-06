package brachy.modularui.utils;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.text.FontRenderHelper;
import brachy.modularui.drawable.text.TextIcon;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A list that lazily parses a list of text-like and drawable types into a vanilla compatible types.
 */
public class TooltipLines extends AbstractList<Either<Component, TooltipComponent>> {

    private final List<Object> elements;
    private final List<Line> lines = new ArrayList<>(8);
    private int lastElementIndex = 0;

    public TooltipLines(List<Object> elements) {
        this.elements = elements;
    }

    public void clearCache() {
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    private void buildUntil(int index) {
        while (index >= lines.size()) {
            Line line = parseNext();
            if (line == null) break;
            lines.add(line);
        }
    }

    private Line parseNext() {
        if (this.lastElementIndex >= elements.size()) return null;
        List<Component> currentLine = new ArrayList<>();
        int currentLength = 0;
        for (int i = this.lastElementIndex; i < this.elements.size(); i++) {
            Object o = elements.get(i);
            currentLength++;
            if (Text.LINE_FEED.equals(o)) {
                if (currentLength == 1 && i > 0 && !Text.LINE_FEED.equals(this.elements.get(i - 1))) {
                    this.lastElementIndex++;
                    currentLength = 0;
                    continue;
                }
                Line line = new Line(collapse(currentLine), this.lastElementIndex, currentLength);
                this.lastElementIndex = i + 1;
                return line;
            }
            Component c = asText(o);
            if (c != null) {
                currentLine.add(c);
                continue;
            }

            if (o instanceof IDrawable drawable && !(o instanceof TooltipComponent)) {
                o = drawable.asIcon();
            }
            if (o instanceof TooltipComponent tc) {
                Line line;
                if (currentLine.isEmpty()) {
                    // a line feed directly after a tooltip component belongs to its line
                    if (i + 1 < this.elements.size() && Text.LINE_FEED.equals(this.elements.get(i + 1))) {
                        currentLength++;
                    }
                    line = new Line(tc, this.lastElementIndex, currentLength);
                    this.lastElementIndex += currentLength;
                } else {
                    // the tooltip component is not part of this line
                    line = new Line(collapse(currentLine), this.lastElementIndex, currentLength - 1);
                    this.lastElementIndex += currentLength - 1;
                }
                return line;
            }
        }
        if (currentLength > 0) {
            Line line = new Line(collapse(currentLine), this.lastElementIndex, currentLength);
            this.lastElementIndex += currentLength;
            return line;
        }
        return null;
    }

    @Nullable
    private static Component asText(Object o) {
        Component c = null;
        if (o instanceof Component txt) {
            c = txt;
        } else if (o instanceof String str) {
            c = Component.literal(str);
        } else if (o instanceof TextIcon ti) {
            c = ti.getText();
        }
        return c != null && !FontRenderHelper.isEmpty(c) ? c : null;
    }

    /**
     * @return true if a line feed must be inserted between two adjacent elements to keep them on separate lines
     */
    private static boolean needsLineFeed(Object prev, Object next) {
        if (Text.LINE_FEED.equals(prev)) return false;
        // text without a line feed would merge with the next line
        if (asText(prev) != null || !(prev instanceof TooltipComponent || prev instanceof IDrawable)) return true;
        // a line feed directly after a tooltip component belongs to its line
        return Text.LINE_FEED.equals(next);
    }

    @Override
    public Either<Component, TooltipComponent> get(int index) {
        buildUntil(index);
        return lines.get(index).text;
    }

    @Override
    public int size() {
        buildUntil(Integer.MAX_VALUE);
        return lines.size();
    }

    @Override
    public Either<Component, TooltipComponent> remove(int index) {
        buildUntil(index);
        Line line = lines.remove(index);

        if (line.length == 1) {
            this.elements.remove(line.index);
        } else {
            this.elements.subList(line.index, line.index + line.length).clear();
        }
        int shift = -line.length;
        if (index > 0 && line.index < this.elements.size() &&
                needsLineFeed(this.elements.get(line.index - 1), this.elements.get(line.index))) {
            // the removed line separated the previous line from the next one
            this.elements.add(line.index, Text.LINE_FEED);
            this.lines.get(index - 1).length++;
            shift++;
        }
        for (int i = index; i < lines.size(); i++) {
            lines.get(i).index += shift;
        }
        this.lastElementIndex += shift;

        return line.text;
    }

    @Override
    public void add(int index, Either<Component, TooltipComponent> s) {
        buildUntil(index);
        int elementIndex = index >= this.lines.size() ? this.lastElementIndex : this.lines.get(index).index;
        Object element = s.map(c -> c, tc -> tc);
        int inserted = 0;
        if (index > 0 && needsLineFeed(this.elements.get(elementIndex - 1), element)) {
            // the previous line has no line feed, so it would merge with this line
            this.elements.add(elementIndex++, Text.LINE_FEED);
            this.lines.get(index - 1).length++;
            inserted++;
        }
        boolean hasNext = elementIndex < this.elements.size();
        this.elements.add(elementIndex, element);
        int length = 1;
        if (hasNext) {
            // separate this line from the next one
            this.elements.add(elementIndex + 1, Text.LINE_FEED);
            length++;
        }
        lines.add(index, new Line(s, elementIndex, length));
        inserted += length;
        for (int i = index + 1; i < this.lines.size(); i++) {
            lines.get(i).index += inserted;
        }
        this.lastElementIndex += inserted;
    }

    @Override
    public Either<Component, TooltipComponent> set(int index, Either<Component, TooltipComponent> element) {
        // remove and add take care of the line feeds between lines
        Either<Component, TooltipComponent> old = remove(index);
        add(index, element);
        return old;
    }

    @Override
    public void clear() {
        this.elements.clear();
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    public List<ClientTooltipComponent> toClientTooltipComponents() {
        buildUntil(Integer.MAX_VALUE);
        return stream()
                .map(either -> either.map(TooltipLines::textToCTC, TooltipLines::tooltipComponentToCTC))
                .toList();
    }

    /**
     * @return a view of this list with the element type vanilla/forge expects. Other mods may insert any
     *         {@link FormattedText} into it, which is converted to a {@link Component}.
     */
    public List<Either<FormattedText, TooltipComponent>> vanillaElementHandler() {
        return new VanillaElementHandler();
    }

    public static ClientTooltipComponent textToCTC(Component text) {
        if (text instanceof ClientTooltipComponent ctc) return ctc;
        return ClientTooltipComponent.create(text.getVisualOrderText());
    }

    public static ClientTooltipComponent tooltipComponentToCTC(TooltipComponent comp) {
        if (comp instanceof ClientTooltipComponentIcon icon) return icon.getClientTooltipComponent();
        if (comp instanceof ClientTooltipComponent ctc) return ctc;
        return ClientTooltipComponent.create(comp);
    }

    private static Component collapse(List<Component> components) {
        if (components.isEmpty()) return Text.EMPTY;
        else if (components.size() == 1) return components.get(0);
        else return Text.comp(components.toArray(Component[]::new));
    }

    // Either is immutable, so a Component on the left is always a valid FormattedText
    @SuppressWarnings("unchecked")
    private static Either<FormattedText, TooltipComponent> upcast(Either<Component, TooltipComponent> either) {
        return (Either<FormattedText, TooltipComponent>) (Either<? extends FormattedText, TooltipComponent>) either;
    }

    private static Either<Component, TooltipComponent> toComponent(Either<FormattedText, TooltipComponent> either) {
        return either.mapLeft(TooltipLines::asComponent);
    }

    private static Component asComponent(FormattedText text) {
        if (text instanceof Component component) return component;
        MutableComponent out = Component.empty();
        text.visit((style, str) -> {
            out.append(Component.literal(str).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    private class VanillaElementHandler extends AbstractList<Either<FormattedText, TooltipComponent>> {

        @Override
        public Either<FormattedText, TooltipComponent> get(int index) {
            return upcast(TooltipLines.this.get(index));
        }

        @Override
        public int size() {
            return TooltipLines.this.size();
        }

        @Override
        public void add(int index, Either<FormattedText, TooltipComponent> element) {
            TooltipLines.this.add(index, toComponent(element));
        }

        @Override
        public Either<FormattedText, TooltipComponent> set(int index, Either<FormattedText, TooltipComponent> element) {
            return upcast(TooltipLines.this.set(index, toComponent(element)));
        }

        @Override
        public Either<FormattedText, TooltipComponent> remove(int index) {
            return upcast(TooltipLines.this.remove(index));
        }

        @Override
        public void clear() {
            TooltipLines.this.clear();
        }
    }

    private static class Line {

        private final Either<Component, TooltipComponent> text;
        private int length;
        private int index;

        private Line(Either<Component, TooltipComponent> text, int index, int length) {
            this.text = text;
            this.index = index;
            this.length = length;
        }

        private Line(Component text, int index, int length) {
            this(Either.left(text), index, length);
        }

        private Line(TooltipComponent text, int index, int length) {
            this(Either.right(text), index, length);
        }
    }
}
