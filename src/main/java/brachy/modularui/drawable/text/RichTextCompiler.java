package brachy.modularui.drawable.text;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.DelegateIcon;
import brachy.modularui.drawable.Icon;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class compiles a list of objects into renderable text. The objects can be strings or any drawable.
 * The compiler will try to inline the drawables into the text according to the given maximum width.
 * Recommended usage is via {@link TextRenderer#compileAndDraw(GuiContext, List)}.
 */
public class RichTextCompiler {

    public static final RichTextCompiler INSTANCE = new RichTextCompiler();

    private static final FormattedCharSequence SPACE = FormattedCharSequence.codepoint(' ', Style.EMPTY);

    private Font fr;
    private int maxWidth;

    private List<ITextLine> lines;
    private List<Object> currentLine;
    private float x, h;

    public List<ITextLine> compileLines(Font fr, List<Object> raw, int maxWidth, float scale) {
        reset(fr, (int) (maxWidth / scale));
        compile(raw);
        return lines;
    }

    public void reset(Font fr, int maxWidth) {
        this.fr = fr != null ? fr : Minecraft.getInstance().font;
        this.maxWidth = maxWidth > 0 ? maxWidth : Integer.MAX_VALUE;
        this.lines = new ArrayList<>();
        this.currentLine = new ArrayList<>();
        this.x = 0;
        this.h = 0;
    }

    private void compile(List<Object> raw) {
        for (Object o : raw) {
            if (o instanceof ITextLine line) {
                newLine();
                this.lines.add(line);
                continue;
            }
            FormattedText text = null;
            if (o instanceof FormattedText ft) {
                text = ft;
            } else if (o instanceof Text key) {
                if (key == Text.EMPTY) continue;
                if (key == Text.SPACE) {
                    MutableComponent mc = key.get();
                    this.x += this.fr.width(mc);
                    continue;
                }
                if (key == Text.LINE_FEED) {
                    newLine();
                    continue;
                }
                text = key.getFormatted();
            } else {
                if (o instanceof ClientTooltipComponent ctc) {
                    o = new ClientTooltipComponentIcon(ctc);
                } else if (o instanceof TooltipComponent tc) {
                    o = new ClientTooltipComponentIcon(tc);
                } else if (!(o instanceof IDrawable)) {
                    text = Component.literal(o.toString());
                }
            }
            if (text != null) {
                compileText(text);
                continue;
            }
            if (!(o instanceof IIcon)) {
                o = ((IDrawable) o).asIcon();// .size(fr.lineHeight);
            }
            IIcon icon = (IIcon) o;
            IIcon delegate = icon;
            if (icon instanceof DelegateIcon di) {
                delegate = di.findRootDelegate();
            }
            if (delegate instanceof Icon icon1) {
                int defaultSize = this.fr.lineHeight;
                // if (icon1.getWidth() <= 0) icon1.width(defaultSize);
                if (icon1.getHeight() <= 0) icon1.height(defaultSize);
            }
            addLineElement(icon);
        }
        newLine();
    }

    private void compileText(FormattedText component) {
        this.fr.split(component, this.maxWidth).forEach(this::addLineElement);
    }

    private void newLine() {
        while (!this.currentLine.isEmpty() && this.currentLine.get(this.currentLine.size() - 1) == SPACE) {
            this.currentLine.remove(this.currentLine.size() - 1); // trims right space
            // TODO trim all left & right space
        }
        int x = (int) Math.ceil(this.x), h = (int) Math.ceil(this.h);
        this.x = 0;
        this.h = 0;
        if (!this.currentLine.isEmpty()) {
            if (this.currentLine.size() == 1 && this.currentLine.get(0) instanceof FormattedCharSequence fcs) {
                // simplest case: one text element
                this.lines.add(new TextLine(fcs, x));
                this.currentLine.clear();
                return;
            }
            // if all elements are text, merge them into one text element, otherwise use a composed line
            List<FormattedCharSequence> fcsList = new ArrayList<>();
            for (Object o : this.currentLine) {
                if (o instanceof FormattedCharSequence fcs) {
                    fcsList.add(fcs);
                } else {
                    this.lines.add(new ComposedLine(this.currentLine, x, h));
                    this.currentLine = new ArrayList<>();
                    return;
                }
            }
            this.lines.add(new TextLine(FormattedCharSequence.fromList(fcsList), x));
            this.currentLine.clear();
        }
    }

    private void addLineElement(IIcon icon) {
        if (icon.getWidth() > this.maxWidth) {
            ModularUI.LOGGER.warn("Icon is wider than max width");
        } else if (this.x + icon.getWidth() > this.maxWidth) {
            newLine();
        }
        this.h = Math.max(this.h, icon.getHeight());
        this.x += icon.getWidth();
        this.currentLine.add(icon);
    }

    private void addLineElement(FormattedCharSequence fcs) {
        if (fcs == FormattedCharSequence.EMPTY) return;
        if (this.currentLine.isEmpty() && fcs == SPACE) return;
        this.x += this.fr.width(fcs);
        this.h = Math.max(this.h, this.fr.lineHeight);
        this.currentLine.add(fcs);
    }

    private void addLineElement(String s, Style style, float width) {
        if (this.currentLine.isEmpty()) s = trimAt(s, 0);
        if (s.isEmpty()) return;
        this.h = Math.max(this.h, this.fr.lineHeight);
        this.x += width;
        this.currentLine.add(Language.getInstance().getVisualOrder(FormattedText.of(s, style)));
    }

    public static String trimRight(String s) {
        int i = s.length() - 1;
        for (; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) break;
        }
        if (i < s.length() - 1) s = s.substring(0, i + 1);
        return s;
    }

    public static String trimAt(String s, int start) {
        int l = 0;
        for (int i = Math.max(0, start), n = s.length(); i < n; i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                l++;
            } else {
                break;
            }
        }
        if (l == 0) return s;
        if (start <= 0) return s.substring(l);
        return s.substring(0, start) + s.substring(start + l);
    }
}
