package brachy.modularui.utils;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.text.FontRenderHelper;
import brachy.modularui.drawable.text.TextIcon;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.mojang.datafixers.util.Either;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A list that lazily parses a list of text-like and drawable types into a vanilla compatible types.
 */
public class TooltipLines extends AbstractList<Either<FormattedText, TooltipComponent>> {

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
        FormattedText currentLine = FormattedText.EMPTY;
        int currentLength = 0;
        for (int i = this.lastElementIndex; i < this.elements.size(); i++) {
            Object o = elements.get(i);
            currentLength++;
            if (o == Text.LINE_FEED) {
                Line line = new Line(currentLine, this.lastElementIndex, currentLength);
                this.lastElementIndex += currentLength;
                return line;
            }
            FormattedText s = null;
            if (o instanceof FormattedText txt) {
                s = txt;
            } else if (o instanceof String str) {
                s = FormattedText.of(str);
            } else if (o instanceof TextIcon ti) {
                s = ti.getText();
            }
            if (s != null) {
                currentLine = FontRenderHelper.isEmpty(currentLine) ? s : FormattedText.composite(currentLine, s);
            }
        }
        if (currentLength > 0) {
            Line line = new Line(currentLine, this.lastElementIndex, currentLength);
            this.lastElementIndex += currentLength;
            return line;
        }
        return null;
    }

    @Override
    public Either<FormattedText, TooltipComponent> get(int index) {
        buildUntil(index);
        return lines.get(index).text;
    }

    @Override
    public int size() {
        buildUntil(Integer.MAX_VALUE);
        return lines.size();
    }

    @Override
    public Either<FormattedText, TooltipComponent> remove(int index) {
        buildUntil(index);
        Line line = lines.remove(index);

        if (line.length == 1) {
            this.elements.remove(line.index);
        } else {
            this.elements.subList(line.index, line.index + line.length).clear();
        }
        for (int i = index; i < lines.size(); i++) {
            lines.get(i).index -= line.length;
        }
        this.lastElementIndex -= line.length;

        return line.text;
    }

    @Override
    public void add(int index, Either<FormattedText, TooltipComponent> s) {
        buildUntil(index);
        int elementIndex = index >= this.lines.size() ? this.lastElementIndex : this.lines.get(index).index;
        lines.add(index, new Line(s, elementIndex, 1));
        for (int i = index + 1; i < this.lines.size(); i++) {
            lines.get(i).index++;
        }
        s.ifLeft(ft -> {
            this.elements.add(elementIndex, ft);
            this.lastElementIndex++;
        });
        // TODO support tooltip component
    }

    public void add(int index, FormattedText s) {
        add(index, Either.left(s));
    }

    public void add(FormattedText s) {
        add(size(), Either.left(s));
    }

    @Override
    public Either<FormattedText, TooltipComponent> set(int index, Either<FormattedText, TooltipComponent> element) {
        Line line = lines.get(index);
        if (line.length == 1) {
            this.elements.set(line.index, element);
            this.lines.set(index, new Line(element, line.index, line.length));
        } else {
            remove(index);
            add(index, element);
        }
        return line.text;
    }

    @Override
    public void clear() {
        this.elements.clear();
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    public List<ClientTooltipComponent> toClientTooltipComponents() {
        return stream()
                .map(either -> either.map(TooltipLines::textToCTC, TooltipLines::tooltipComponentToCTC))
                .toList();
    }

    public static ClientTooltipComponent textToCTC(FormattedText text) {
        if (text instanceof ClientTooltipComponent ctc) return ctc;
        if (text instanceof Component component) {
            return ClientTooltipComponent.create(component.getVisualOrderText());
        }
        return ClientTooltipComponent.create(Language.getInstance().getVisualOrder(text));
    }

    public static ClientTooltipComponent tooltipComponentToCTC(TooltipComponent comp) {
        if (comp instanceof ClientTooltipComponent ctc) return ctc;
        return ClientTooltipComponent.create(comp);
    }

    private static class Line {

        private final Either<FormattedText, TooltipComponent> text;
        private final int length;
        private int index;

        private Line(Either<FormattedText, TooltipComponent> text, int index, int length) {
            this.text = text;
            this.index = index;
            this.length = length;
        }

        private Line(FormattedText text, int index, int length) {
            this(Either.left(text), index, length);
        }

        private Line(TooltipComponent text, int index, int length) {
            this(Either.right(text), index, length);
        }
    }
}
