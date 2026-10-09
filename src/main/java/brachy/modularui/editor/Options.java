package brachy.modularui.editor;

public interface Options<T> extends Iterable<Option<T, ?>> {

    Option<T, ?> getOption(String name);
}
