package brachy.modularui.widgets;

import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.dynamic.IDynamicHandler;

import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * @deprecated use {@link brachy.modularui.widgets.dynamic.DynamicWidget} instead.
 */
@Deprecated(forRemoval = true)
public class DynamicSyncedWidget<W extends DynamicSyncedWidget<W>> extends Widget<W> {

    private IDynamicHandler syncHandler;
    private IWidget child;

    @Override
    public boolean isValidSyncOrValue(@NonNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(IDynamicHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NonNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castNullable(IDynamicHandler.class);
        if (this.syncHandler != null) this.syncHandler.attachDynamicWidgetListener(this::updateChild);
    }

    @Override
    public @NonNull List<IWidget> getChildren() {
        if (this.child == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(this.child);
        }
    }

    private void updateChild(IWidget widget) {
        if (this.child != null) {
            this.child.dispose();
        } else if (widget == null) {
            return;
        }
        this.child = widget;
        if (isValid()) {
            if (this.child != null) this.child.initialise(this, true);
            scheduleResize();
        }
    }

    public @NonNull IDynamicHandler getDynamicSyncHandler() {
        return syncHandler;
    }

    public W syncHandler(DynamicSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return getThis();
    }

    public W syncHandler(DynamicLinkedSyncHandler<?> syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return getThis();
    }

    /**
     * Sets an initial child. This can only be done before the widget is initialised.
     *
     * @param child initial child
     * @return this
     */
    public W initialChild(IWidget child) {
        if (isValid()) throw new IllegalStateException("Can only set initial child before the widget is initialised.");
        this.child = child;
        return getThis();
    }
}
