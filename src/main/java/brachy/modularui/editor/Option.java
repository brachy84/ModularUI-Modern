package brachy.modularui.editor;

import brachy.modularui.api.widget.IWidget;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface Option<T, V> {

    String name();

    void setField(T holder, V value);

    V getField(T holder);

    boolean canRead();

    boolean canWrite();

    @Nullable
    IDisplayOption<V> display();

    default boolean hasDisplay() {
        return display() != null;
    }

    @Nullable
    default IWidget createDisplay(T holder) {
        return hasDisplay() ? Objects.requireNonNull(display()).createDisplay(this, holder) : null;
    }
}
