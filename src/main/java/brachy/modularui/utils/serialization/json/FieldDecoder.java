package brachy.modularui.utils.serialization.json;

public interface FieldDecoder<T, V> {

    V decodeField(T holder);
}
