package brachy.modularui.utils.serialization.json;

public interface FieldEncoder<T, V> {

    void encodeField(T holder, V value);
}
