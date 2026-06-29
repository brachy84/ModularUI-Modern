package brachy.modularui.editor;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.CustomModularScreen;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Color;
import brachy.modularui.widget.SplitView;
import brachy.modularui.widgets.layout.Flow;

import org.jetbrains.annotations.NotNull;

public class EditorScreen extends CustomModularScreen {

    private ModularPanel<?> edited;
    private IWidget selectedWidget;

    public EditorScreen(@NotNull String owner) {
        super(owner);
    }

    @Override
    public @NotNull ModularPanel<?> buildUI(ModularGuiContext context) {
        var panel = new ModularPanel<>("editor").full().padding(4);


        return panel.child(new SplitView<>(GuiAxis.Y)
                .full()
                .splitValue(0.2f)
                .startWidget(buildWidgetTreeView())
                .endWidget(new SplitView<>(GuiAxis.Y)
                        .splitValue(0.75f)
                        .startWidget(new SplitView<>(GuiAxis.X)
                                .splitValue(0.75f)
                                .startWidget(buildPreview())
                                .endWidget(buildLibraryTabs()))
                        .endWidget(buildWidgetConfigurator())));
    }

    protected IWidget buildWidgetTreeView() {

        return Flow.col()
                .background(new Rectangle().color(Color.RED.brighter(1)))
                .child(Text.str("Widget Tree").asWidget());
    }

    protected IWidget buildPreview() {

        return Flow.col()
                .background(new Rectangle().color(Color.YELLOW.brighter(1)))
                .child(Text.str("Preview").asWidget());
    }

    protected IWidget buildLibraryTabs() {

        return Flow.col()
                .background(new Rectangle().color(Color.BLUE.brighter(1)))
                .child(Text.str("Library").asWidget());
    }

    protected IWidget buildWidgetConfigurator() {

        return Flow.col()
                .background(new Rectangle().color(Color.GREEN.brighter(1)))
                .child(Text.str("Widget Config").asWidget());
    }

    @Override
    public int getGuiScaleOverride() {
        return 2;
    }
}
