package brachy.modularui.editor;

public interface Options<T> extends Iterable<Option<T, ?>> {

    <V> Option<T, V> getOption(String name);
}
