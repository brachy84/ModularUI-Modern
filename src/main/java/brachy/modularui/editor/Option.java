package brachy.modularui.editor;

public interface Option<T, V> {

    String name();

    void setField(T holder, V value);

    V getField(T holder);

    boolean canRead();

    boolean canWrite();
}
