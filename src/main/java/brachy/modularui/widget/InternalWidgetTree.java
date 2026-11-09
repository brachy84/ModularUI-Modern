package brachy.modularui.widget;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.layout.IViewport;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.sizer.ResizeNode;
import brachy.modularui.widgets.layout.IExpander;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;

import com.google.common.base.Joiner;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class InternalWidgetTree {

    static <T extends IWidget> DataResult<T> findChildAt(IWidget parent, Class<T> type, String[] path) {
        if (path.length == 0) return DataResult.error(() -> "Path is empty");
        DataResult<IWidget> next = null;
        for (int i = 0; i < path.length; i++) {
            next = findChild(parent.getChildren(), path[i], i == path.length - 1 ? type : null);
            var res = next.result();
            if (res.isPresent()) {
                parent = res.get();
                continue;
            }
            return next.map(w -> (T) w);
        }
        return next.map(w -> (T) w);
    }

    static <T extends IWidget> DataResult<Pair<T, IWidget>> findChildAndParentAt(IWidget parent, Class<T> type, String[] path) {
        if (path.length == 0) return DataResult.error(() -> "Path is empty");
        IWidget targetParent = null;
        DataResult<IWidget> next = null;
        for (int i = 0; i < path.length; i++) {
            targetParent = parent;
            next = findChild(parent.getChildren(), path[i], i == path.length - 1 ? type : null);
            var res = next.result();
            if (res.isPresent()) {
                parent = res.get();
                continue;
            }
            IWidget finalTargetParent = targetParent;
            return next.map(w -> Pair.of((T) w, finalTargetParent));
        }
        IWidget finalTargetParent = targetParent;
        return next.map(w -> Pair.of((T) w, finalTargetParent));
    }

    static DataResult<IWidget> findChild(List<IWidget> children, String name, Class<?> type) {
        if (name.startsWith("#")) {
            name = name.substring(1);
            if (name.isEmpty()) return DataResult.error(() -> "No index or widget type given");
            if (Character.isDigit(name.charAt(0))) {
                try {
                    int index = Integer.parseInt(name);
                    if (index > 0 && index < children.size()) {
                        var w = children.get(index);
                        if (type == null || type.isInstance(w)) {
                            return DataResult.success(w);
                        }
                        return DataResult.error(() -> "A widget was found the index, but it does not have the required type");
                    } else {
                        return DataResult.error(() -> "Supplied index '%s' is out of bounds [0,%s).".formatted(index, children.size()));
                    }
                } catch (NumberFormatException ignored) {
                    String finalName = name;
                    return DataResult.error(() -> "Error parsing index. %s is not a number.".formatted(finalName));
                }
            }
            var wType = WidgetRegistry.INSTANCE.getNullable(name);
            if (wType == null) {
                String finalName = name;
                return DataResult.error(() -> "No widget type for %s was found".formatted(finalName));
            }
            for (IWidget child : children) {
                if (child.getType() == wType && (type == null || type.isInstance(child))) {
                    return DataResult.success(child);
                }
            }
            return DataResult.error(() -> "No widget of type %s was found".formatted(wType.name()));
        }
        for (IWidget child : children) {
            if (name.equals(child.getName()) && (type == null || type.isInstance(child))) {
                return DataResult.success(child);
            }
        }
        String finalName = name;
        return DataResult.error(() -> "No child of name %s was found".formatted(finalName));
    }

    static void drawTree(IWidget parent, ModularGuiContext context, boolean ignoreEnabled,
                         boolean shouldDrawBackground) {
        if (!parent.isEnabled() && !ignoreEnabled) return;
        if (parent.requiresResize()) {
            WidgetTree.resizeInternal(parent.resizer(), false);
        }

        GuiGraphics graphics = context.getGraphics();
        float alpha = parent.getPanel().getAlpha();
        IViewport viewport = parent instanceof IViewport ? (IViewport) parent : null;

        // transform stack according to the widget
        context.pushMatrix();
        parent.transform(context);

        boolean canBeSeen = parent.canBeSeen(context);

        // apply transformations to opengl
        graphics.pose().pushPose();
        context.applyTo(graphics.pose());

        if (canBeSeen) {
            // draw widget
            graphics.setColor(1f, 1f, 1f, alpha);
            WidgetThemeEntry<?> widgetTheme = parent.getWidgetTheme(parent.getPanel().getTheme());
            if (shouldDrawBackground) parent.drawBackground(context, widgetTheme);
            parent.draw(context, widgetTheme);
            parent.drawOverlay(context, widgetTheme);
        }

        if (viewport != null) {
            if (canBeSeen) {
                // draw viewport without children transformation
                graphics.setColor(1f, 1f, 1f, alpha);
                viewport.preDraw(context, false);
                graphics.pose().popPose();
                // apply children transformation of the viewport
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
                // apply to opengl and draw with transformation
                graphics.pose().pushPose();
                context.applyTo(graphics.pose());
                viewport.preDraw(context, true);
            } else {
                // only transform stack
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
        }
        // remove all opengl transformations
        graphics.pose().popPose();

        // render all children if there are any
        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            boolean backgroundSeparate = children.size() > 1;
            // draw all backgrounds first if we have more than 1 child
            // the whole reason this exists is because of the hover animation of items with NEA
            // on hover the item scales up slightly, this causes the amount text to overlap nearby slots, but since the
            // whole slot is drawn
            // at once the backgrounds might draw on top of the text
            // for now we'll apply this always without checking for NEA as it might be useful for other things
            // maybe proper layer customization in the future?
            if (backgroundSeparate) children.forEach(widget -> drawBackground(widget, context, ignoreEnabled));
            children.forEach(widget -> drawTree(widget, context, false, !backgroundSeparate));
        }

        if (viewport != null) {
            if (canBeSeen) {
                // apply opengl transformations again and draw
                graphics.setColor(1f, 1f, 1f, alpha);
                graphics.pose().pushPose();
                context.applyTo(graphics.pose());
                viewport.postDraw(context, true);
                // remove children transformation of this viewport
                context.popViewport(viewport);
                graphics.pose().popPose();
                // apply transformation again to opengl and draw
                graphics.pose().pushPose();
                context.applyTo(graphics.pose());
                viewport.postDraw(context, false);
                graphics.pose().popPose();
            } else {
                // only remove transformation
                context.popViewport(viewport);
            }
        }
        // remove all widget transformations
        context.popMatrix();
    }

    static void drawBackground(IWidget parent, ModularGuiContext context, boolean ignoreEnabled) {
        if (!parent.isEnabled() && !ignoreEnabled) return;

        GuiGraphics graphics = context.getGraphics();
        float alpha = parent.getPanel().getAlpha();

        // transform stack according to the widget
        context.pushMatrix();
        parent.transform(context);

        boolean canBeSeen = parent.canBeSeen(context);
        if (!canBeSeen) {
            context.popMatrix();
            return;
        }

        // apply transformations to opengl
        graphics.pose().pushPose();
        context.applyTo(graphics.pose());

        // draw widget
        graphics.setColor(1f, 1f, 1f, alpha);
        WidgetThemeEntry<?> widgetTheme = parent.getWidgetTheme(parent.getPanel().getTheme());
        parent.drawBackground(context, widgetTheme);

        graphics.pose().popPose();
        context.popMatrix();
    }

    static void drawTreeForeground(IWidget parent, ModularGuiContext context) {
        IViewport viewport = parent instanceof IViewport viewport1 ? viewport1 : null;
        context.pushMatrix();
        parent.transform(context);

        context.getGraphics().setColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        parent.drawForeground(context);

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            if (viewport != null) {
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
            children.forEach(widget -> drawTreeForeground(widget, context));
            if (viewport != null) context.popViewport(viewport);
        }
        context.popMatrix();
    }

    static boolean resize(ResizeNode resizer, boolean init, boolean onOpen, boolean isParentLayout) {
        boolean alreadyCalculated = false;
        // first try to resize this widget
        boolean isLayout = resizer.isLayout();
        if (init) {
            resizer.initResizing(onOpen);
            if (!isLayout) resizer.setLayoutDone(true);
        } else {
            // if this is not the first time check if this widget is already resized
            alreadyCalculated = resizer.isFullyCalculated(isParentLayout);
        }
        boolean selfFullyCalculated = resizer.isSelfFullyCalculated() || resizer.resize(isParentLayout);

        GuiAxis expandAxis = resizer instanceof IExpander expander ? expander.getExpandAxis() : null;
        // now resize all children and collect children which could not be fully calculated
        List<ResizeNode> anotherResize = Collections.emptyList();
        if (!resizer.areChildrenCalculated() && !resizer.getChildren().isEmpty()) {
            anotherResize = new ArrayList<>();
            for (ResizeNode child : resizer.getChildren()) {
                if (init) child.checkExpanded(expandAxis);
                if (!resize(child, init, onOpen, isLayout)) {
                    anotherResize.add(child);
                }
            }
        }

        boolean shouldLayout = init || !resizer.areChildrenCalculated() || !resizer.isLayoutDone();
        if (shouldLayout || !selfFullyCalculated) {
            boolean layoutSuccessful = true;
            // we need to keep track of which widgets are not yet fully calculated, so we can call onResized on those
            // which later are fully calculated
            BitSet state = getCalculatedState(anotherResize, isLayout);
            if (isLayout && shouldLayout) {
                layoutSuccessful = resizer.layoutChildren();
            }

            // post resize this widget if possible
            resizer.postResize();

            if (isLayout && shouldLayout) {
                layoutSuccessful &= resizer.postLayoutChildren();
                if (!selfFullyCalculated) resizer.postResize();
            }
            if (shouldLayout) resizer.setLayoutDone(layoutSuccessful);
            checkFullyCalculated(anotherResize, state, isLayout);
        }

        // now fully resize all children which needs it
        if (!anotherResize.isEmpty()) {
            for (int i = 0; i < anotherResize.size(); i++) {
                if (resize(anotherResize.get(i), false, onOpen, isLayout)) {
                    anotherResize.remove(i--);
                }
            }
        }
        resizer.setChildrenResized(anotherResize.isEmpty());
        selfFullyCalculated = resizer.isFullyCalculated(isParentLayout);

        if (selfFullyCalculated && !alreadyCalculated) resizer.onResized();

        return selfFullyCalculated;
    }

    private static BitSet getCalculatedState(List<ResizeNode> children, boolean isLayout) {
        if (children.isEmpty()) return null;
        BitSet state = new BitSet();
        for (int i = 0; i < children.size(); i++) {
            ResizeNode widget = children.get(i);
            if (widget.isFullyCalculated(isLayout)) {
                state.set(i);
            }
        }
        return state;
    }

    private static void checkFullyCalculated(List<ResizeNode> children, BitSet state, boolean isLayout) {
        if (children.isEmpty() || state == null) return;
        int j = 0;
        for (int i = 0; i < children.size(); i++) {
            ResizeNode widget = children.get(i);
            if (!state.get(j) && widget.isFullyCalculated(isLayout)) {
                widget.onResized();
                state.set(j);
                children.remove(i--);
            }
            j++;
        }
    }
}
