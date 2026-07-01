package brachy.modularui.editor;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IDragHandle;
import brachy.modularui.api.widget.IDraggable;
import brachy.modularui.api.widget.IParentWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.DrawableRegistry;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.CustomModularScreen;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.EnumValue;
import brachy.modularui.value.FloatValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.widget.RecursiveDelegatingWidget;
import brachy.modularui.widget.SingleChildWidget;
import brachy.modularui.widget.SplitView;
import brachy.modularui.widget.WidgetRegistry;
import brachy.modularui.widget.WidgetType;
import brachy.modularui.widget.sizer.StandardResizer;
import brachy.modularui.widget.sizer.Unit;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.CollapsableList;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.PageButton;
import brachy.modularui.widgets.PagedWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.draggable.AbstractDraggable;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import com.mojang.blaze3d.platform.InputConstants;

import com.google.common.base.CaseFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorScreen extends CustomModularScreen {

    private ModularPanel<?> edited;
    private WidgetWrapper editedWrapper;
    private WidgetWrapper selectedWidget;
    private Flow widgetConfigurator;
    private ListWidget<IWidget, ?> widgetTree;

    public EditorScreen(@NotNull String owner) {
        super(owner);
    }

    @Override
    public @NotNull ModularPanel<?> buildUI(ModularGuiContext context) {
        this.edited = new ModularPanel<>("editing").child(new ButtonWidget<>().center().size(50));
        this.editedWrapper = new WidgetWrapper(this.edited);

        var panel = new ModularPanel<>("editor").full().padding(4).disableThemeBackground(true).disableHoverThemeBackground(true);
        this.widgetConfigurator = Flow.col()
                .background(new Rectangle().color(Color.withAlpha(Color.GREEN.brighter(1), 0.7f)))
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .child(Text.str("Widget Config").asWidget().color(Color.WHITE.main));
        buildWidgetConfigurator();
        this.widgetTree = ListWidget.simple().crossAxisAlignment(Alignment.CrossAxis.START);
        buildWidgetTreeView();

        return panel.child(new SplitView<>(GuiAxis.Y)
                .full()
                .splitValue(0.2f)
                .startWidget(Flow.col()
                        .background(new Rectangle().color(Color.withAlpha(Color.RED.brighter(1), 0.7f)))
                        .child(Text.str("Widget Tree").asWidget().color(Color.WHITE.main))
                        .child(this.widgetTree.expanded().widthRel(1f)))
                .endWidget(new SplitView<>(GuiAxis.Y)
                        .splitValue(0.75f)
                        .startWidget(new SplitView<>(GuiAxis.X)
                                .splitValue(0.75f)
                                .startWidget(buildPreview())
                                .endWidget(buildLibraryTabs()))
                        .endWidget(this.widgetConfigurator)));
    }

    protected void buildWidgetTreeView() {
        this.widgetTree.removeAll();
        this.widgetTree.child(addTreeViewNodes(this.editedWrapper));
        this.widgetTree.scheduleResize();
    }

    private IWidget addTreeViewNodes(WidgetWrapper parent) {
        if (!parent.hasChildren()) {
            return new TreeViewNode(parent);
        }
        var w = new CollapsableList();
        w.title(new TreeViewNode(parent));
        for (WidgetWrapper child : parent.getTypedChildren()) {
            w.child(addTreeViewNodes(child));
        }
        return w;
    }

    protected IWidget buildPreview() {

        return Flow.col()
                //.background(new Rectangle().color(Color.YELLOW.brighter(1)))
                .child(this.editedWrapper);
    }

    protected IWidget buildLibraryTabs() {

        var pageController = new PagedWidget.Controller();
        return Flow.col()
                .background(new Rectangle().color(Color.withAlpha(Color.BLUE.brighter(1), 0.7f)))
                .child(Flow.row()
                        .coverChildren()
                        .left(0)
                        .child(new PageButton(0, pageController).size(60, 14).overlay(Text.str("Widgets")))
                        .child(new PageButton(1, pageController).size(60, 14).overlay(Text.str("Drawables")))
                )
                .child(new PagedWidget<>()
                        .expanded()
                        .fullWidth()
                        .controller(pageController)
                        .addPage(ListWidget.simple()
                                .full()
                                .child(Flow.row().wrap()
                                        .coverChildrenHeight()
                                        .childPadding(2)
                                        .children(WidgetRegistry.INSTANCE, t -> t.hasCreator() ? new LibraryWidget(t) : null)))
                        .addPage(ListWidget.simple()
                                .full()
                                .child(Flow.row().wrap()
                                        .coverChildrenHeight()
                                        .childPadding(2)
                                        .children(DrawableRegistry.INSTANCE, t -> Text.str(t.name()).asWidget().size(60).color(Color.WHITE.main)))));
    }

    protected void buildWidgetConfigurator() {
        var cfg = this.widgetConfigurator;
        while (cfg.getChildren().size() > 1) {
            cfg.remove(-1);
        }
        if (this.selectedWidget != null) {
            var w = this.selectedWidget.getDelegate();
            cfg.child(Text.str(w.getTypeName()).asWidget().color(Color.WHITE.main).horizontalCenter());
            buildSizePosConfig(w);
            return;
        }
        cfg.child(Text.str("No widget selected").asWidget().color(Color.WHITE.main));
        cfg.scheduleResize();
    }

    private void buildSizePosConfig(IWidget widget) {
        var cfg = this.widgetConfigurator;
        cfg.child(Text.str("Size and Position").asWidget());
        var flow = Flow.col().crossAxisAlignment(Alignment.CrossAxis.START)
                .fullWidth()
                .coverChildrenHeight()
                .childPadding(3)
                .collapseDisabledChildren();
        cfg.child(flow);
        var type = widget.getType();
        var left = type.getOptions().getOption("left");
        var right = type.getOptions().getOption("right");
        var width = type.getOptions().getOption("width");
        var top = type.getOptions().getOption("top");
        var bottom = type.getOptions().getOption("bottom");
        var height = type.getOptions().getOption("height");
        var r = widget.resizer();
        int x = 0, y = 0;
        if (posCard(flow, widget, r, GuiAxis.X, Unit.State.START, (Option<IWidget, Unit>) left)) x++;
        if (posCard(flow, widget, r, GuiAxis.X, Unit.State.END, (Option<IWidget, Unit>) right)) x++;
        if (sizeCard(flow, widget, r, GuiAxis.X, (Option<IWidget, Unit>) width)) x++;
        if (posCard(flow, widget, r, GuiAxis.Y, Unit.State.START, (Option<IWidget, Unit>) top)) y++;
        if (posCard(flow, widget, r, GuiAxis.Y, Unit.State.END, (Option<IWidget, Unit>) bottom)) y++;
        if (sizeCard(flow, widget, r, GuiAxis.Y, (Option<IWidget, Unit>) height)) y++;

    }

    private boolean posCard(Flow parent, IWidget widget, StandardResizer resizer, GuiAxis axis, Unit.State state, Option<IWidget, Unit> unit) {
        Option<Unit, Float> valueOption = (Option<Unit, Float>) Unit.FULL_CODEC.getOption("value");
        Option<Unit, Unit.Measure> measureOption = (Option<Unit, Unit.Measure>) Unit.FULL_CODEC.getOption("measure");
        Option<Unit, Integer> offsetOption = (Option<Unit, Integer>) Unit.FULL_CODEC.getOption("offset");
        Option<Unit, Float> anchorOption = (Option<Unit, Float>) Unit.FULL_CODEC.getOption("anchor");
        Option<Unit, Boolean> autoAnchorOption = (Option<Unit, Boolean>) Unit.FULL_CODEC.getOption("autoAnchor");
        var customOffset = new BoolValue(false);
        var card = Flow.col().name(unit.name() + "_config_card")
                .coverChildrenHeight()
                .padding(2)
                .childPadding(1)
                .setEnabledIf(f -> resizer.has(axis, state))
                .collapseDisabledChildren()
                .child(Flow.row().name("header")
                        .coverChildrenHeight()
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(Text.str(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, unit.name())).asWidget().name("name"))
                        .child(new ButtonWidget<>().name("remove_button")
                                .invisible()
                                .overlay(GuiTextures.REMOVE)
                                .size(12)
                                .onMousePressed((ctx, button) -> {
                                    resizer.remove(axis, state);
                                    return true;
                                })))
                .child(Flow.row()
                        .fullWidth()
                        .height(14)
                        .childPadding(2)
                        .child(new CycleButtonWidget()
                                .expanded()
                                .fullHeight()
                                .stateCount(2)
                                .value(new IntValue.Dynamic(() -> get(widget, unit, measureOption, Unit.Measure.PIXEL).ordinal(), v -> {
                                    var m = v == 0 ? Unit.Measure.PIXEL : Unit.Measure.RELATIVE;
                                    if (m != Unit.Measure.RELATIVE) {
                                        set(widget, unit, offsetOption, 0);
                                    }
                                    set(widget, unit, measureOption, m);
                                }))
                                .stateOverlay(Unit.Measure.PIXEL, Text.str("Pixel"))
                                .stateOverlay(Unit.Measure.RELATIVE, Text.str("Relative")))
                        .child(new TextFieldWidget()
                                .expanded()
                                .fullHeight()
                                .setNumbersDouble(d -> d)
                                .value(new FloatValue.Dynamic(() -> {
                                    float v = get(widget, unit, valueOption, 0f);
                                    return get(widget, unit, measureOption, Unit.Measure.PIXEL) == Unit.Measure.PIXEL ? Math.round(v) : Math.round(v * 1000) / 1000f;
                                }, v -> {
                                    set(widget, unit, valueOption, v);
                                }))))
                .child(Flow.row().name("offset_row")
                        .height(14)
                        .childPadding(2)
                        .setEnabledIf(f -> get(widget, unit, measureOption, Unit.Measure.PIXEL) == Unit.Measure.RELATIVE)
                        .child(Text.str("Offset").asWidget())
                        .child(new ToggleButton()
                                .size(14)
                                .invisible()
                                .stateOverlay(GuiTextures.CHECK_BOX)
                                .value(new BoolValue.Dynamic(customOffset::getBoolValue, v -> {
                                    customOffset.setBoolValue(v);
                                    set(widget, unit, offsetOption, 0);
                                })))
                        .child(new TextFieldWidget()
                                .expanded()
                                .fullHeight()
                                .setEnabledIf(w -> customOffset.getBoolValue())
                                .setNumbers(d -> d)
                                .value(new IntValue.Dynamic(() -> get(widget, unit, offsetOption, 0), v -> {
                                    set(widget, unit, offsetOption, v);
                                }))))
                .child(Flow.row().name("anchor_row")
                        .height(14)
                        .childPadding(2)
                        .setEnabledIf(f -> get(widget, unit, measureOption, Unit.Measure.PIXEL) == Unit.Measure.RELATIVE)
                        .child(Text.str("Anchor").asWidget())
                        .child(new ToggleButton()
                                .size(14)
                                .invisible()
                                .stateOverlay(GuiTextures.CHECK_BOX)
                                .value(new BoolValue.Dynamic(() -> !get(widget, unit, autoAnchorOption, true), v -> {
                                    set(widget, unit, autoAnchorOption, !v);
                                })))
                        .child(new TextFieldWidget()
                                .expanded()
                                .fullHeight()
                                .setEnabledIf(w -> !get(widget, unit, autoAnchorOption, true))
                                .setNumbersDouble(d -> d)
                                .value(new FloatValue.Dynamic(() -> {
                                    float v = get(widget, unit, anchorOption, 0f);
                                    return Math.round(v * 1000) / 1000f;
                                }, v -> {
                                    set(widget, unit, anchorOption, v);
                                }))));
        parent.child(card);
        return resizer.has(axis, state);
    }

    private boolean sizeCard(Flow parent, IWidget widget, StandardResizer resizer, GuiAxis axis, Option<IWidget, Unit> unit) {
        var state = Unit.State.SIZE;
        Option<Unit, Float> valueOption = (Option<Unit, Float>) Unit.FULL_CODEC.getOption("value");
        Option<Unit, Unit.Measure> measureOption = (Option<Unit, Unit.Measure>) Unit.FULL_CODEC.getOption("measure");
        Option<Unit, Integer> offsetOption = (Option<Unit, Integer>) Unit.FULL_CODEC.getOption("offset");
        var customOffset = new BoolValue(false);
        var card = Flow.col().name(unit.name() + "_config_card")
                .coverChildrenHeight()
                .padding(2)
                .childPadding(1)
                .setEnabledIf(f -> resizer.has(axis, state))
                .collapseDisabledChildren()
                .child(Flow.row().name("header")
                        .coverChildrenHeight()
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(Text.str(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, unit.name())).asWidget().name("name"))
                        .child(new ButtonWidget<>().name("remove_button")
                                .invisible()
                                .overlay(GuiTextures.REMOVE)
                                .size(12)
                                .onMousePressed((ctx, button) -> {
                                    resizer.remove(axis, state);
                                    return true;
                                })))
                .child(Flow.row().name("value_row")
                        .fullWidth()
                        .height(14)
                        .childPadding(2)
                        .child(new CycleButtonWidget()
                                .expanded()
                                .fullHeight()
                                .value(new EnumValue.Dynamic<>(Unit.Measure.class, () -> get(widget, unit, measureOption, Unit.Measure.PIXEL), m -> {
                                    if (m == Unit.Measure.PIXEL) {
                                        set(widget, unit, offsetOption, 0);
                                    }
                                    set(widget, unit, measureOption, m);
                                }))
                                .stateOverlay(Unit.Measure.PIXEL, Text.str("Pixel"))
                                .stateOverlay(Unit.Measure.RELATIVE, Text.str("Relative"))
                                .stateOverlay(Unit.Measure.COVER_CHILDREN, Text.str("Cover children")))
                        .child(new TextFieldWidget()
                                .expanded()
                                .fullHeight()
                                .setNumbersDouble(d -> d)
                                .value(new FloatValue.Dynamic(() -> {
                                    float v = get(widget, unit, valueOption, 0f);
                                    return get(widget, unit, measureOption, Unit.Measure.PIXEL) == Unit.Measure.PIXEL ? Math.round(v) : Math.round(v * 1000) / 1000f;
                                }, v -> {
                                    set(widget, unit, valueOption, v);
                                }))))
                .child(Flow.row().name("offset_row")
                        .height(14)
                        .childPadding(2)
                        .setEnabledIf(f -> get(widget, unit, measureOption, Unit.Measure.PIXEL) == Unit.Measure.RELATIVE)
                        .child(Text.str("Offset").asWidget())
                        .child(new ToggleButton()
                                .size(14)
                                .invisible()
                                .stateOverlay(GuiTextures.CHECK_BOX)
                                .value(new BoolValue.Dynamic(customOffset::getBoolValue, v -> {
                                    customOffset.setBoolValue(v);
                                    set(widget, unit, offsetOption, 0);
                                })))
                        .child(new TextFieldWidget()
                                .expanded()
                                .fullHeight()
                                .setNumbersDouble(d -> d)
                                .value(new IntValue.Dynamic(() -> get(widget, unit, offsetOption, 0), v -> {
                                    set(widget, unit, offsetOption, v);
                                }))));
        parent.child(card);
        return resizer.has(axis, state);
    }

    private <V> void set(IWidget widget, Option<IWidget, Unit> o1, Option<Unit, V> o2, V value) {
        var u = o1.getField(widget);
        if (u != null) {
            o2.setField(u, value);
            widget.scheduleResize();
        }
    }

    private <V> V get(IWidget widget, Option<IWidget, Unit> o1, Option<Unit, V> o2, V defaultValue) {
        var u = o1.getField(widget);
        if (u != null) {
            return o2.getField(u);
        }
        return defaultValue;
    }

    public void updateSelected(WidgetWrapper wrapper) {
        this.selectedWidget = wrapper;
        buildWidgetConfigurator();
    }

    @Override
    public int getGuiScaleOverride() {
        return 2;
    }

    public static class LibraryWidget extends TextWidget<LibraryWidget> implements IDragHandle {

        private final WidgetType<?> type;

        public LibraryWidget(WidgetType<?> type) {
            super(Text.str(type.name()).color(Color.WHITE.main));
            this.type = type;
            size(60);
            textAlign(Alignment.CENTER);
        }

        @Override
        public @Nullable IDraggable createDraggable(ModularGuiContext ctx, int button) {
            return new WidgetDraggable(this.type);
        }
    }

    public static class WidgetDraggable extends AbstractDraggable {

        private final WidgetType<?> type;
        private final IDrawable icon;

        public WidgetDraggable(WidgetType<?> type) {
            this.type = type;
            this.icon = Text.str(type.name());
            getMovingArea().setSize(60, 60);
        }

        @Override
        public void drawMovingState(ModularGuiContext context, float partialTicks) {
            this.icon.draw(context, getMovingArea(), WidgetTheme.getDefault().theme());
        }

        @Override
        public void onDragEnd(ModularGuiContext context) {
            var hovered = context.getTopHovered();
            if (hovered instanceof WidgetWrapper ww) {
                if (ww.getDelegate() instanceof IParentWidget<?, ?> parent) {
                    parent.addChildRaw(this.type.createNewInstance(), -1);
                    if (ww.getScreen() instanceof EditorScreen editorScreen) {
                        editorScreen.buildWidgetTreeView();
                    }
                } else if (ww.getDelegate() instanceof SingleChildWidget<?> singleChildWidget) {
                    singleChildWidget.child(this.type.createNewInstance());
                    if (ww.getScreen() instanceof EditorScreen editorScreen) {
                        editorScreen.buildWidgetTreeView();
                    }
                }
            }
        }
    }

    public static class WidgetWrapper extends RecursiveDelegatingWidget<WidgetWrapper> implements Interactable {

        public static final IDrawable OUTLINE = new IDrawable() {
            @Override
            public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
                GuiDraw.drawBorderOutsideXYWH(context.getGraphics(), x, y, width, height, 2f, Color.RED.main);
            }
        };

        public WidgetWrapper(IWidget delegate) {
            super(delegate);
        }

        @Override
        protected WidgetWrapper createChildDelegate(IWidget widget) {
            return new WidgetWrapper(widget);
        }

        public boolean isSelected() {
            return getScreen() instanceof EditorScreen editorScreen && editorScreen.selectedWidget == this;
        }

        @Override
        public void drawForeground(ModularGuiContext context) {
            if (isSelected()) {
                context.graphicsPose().pushPose();
                context.applyTo(context.graphicsPose());
                OUTLINE.drawAtZero(context, getArea(), WidgetTheme.getDefault().theme());
                context.graphicsPose().popPose();
            }
            super.drawForeground(context);
        }


        @Override
        public @NotNull Result onMousePressed(int button) {
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                if (getScreen() instanceof EditorScreen editorScreen) {
                    editorScreen.updateSelected(this);
                }
                return Result.SUCCESS;
            }
            return Result.ACCEPT;
        }
    }

    public static class TreeViewNode extends TextWidget<TreeViewNode> implements Interactable {

        private final WidgetWrapper widget;

        public TreeViewNode(WidgetWrapper wrapper) {
            super(Text.str(wrapper.getDelegate().getTypeName()));
            this.widget = wrapper;
            color(Color.WHITE.main);
            padding(2);
        }

        @Override
        public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            super.drawBackground(context, widgetTheme);
            if (this.widget.isSelected()) {
                var a = getArea();
                GuiDraw.drawRect(context.getGraphics(), 0, 0, a.width, a.height, Color.withAlpha(Color.WHITE.main, 0.2f));
            }
        }

        @Override
        public @NotNull Result onMousePressed(int button) {
            return this.widget.onMousePressed(button);
        }
    }
}
