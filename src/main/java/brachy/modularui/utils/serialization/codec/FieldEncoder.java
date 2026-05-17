package brachy.modularui.utils.serialization.codec;

public interface FieldEncoder<T, V> {

    void encodeField(T holder, V value);
}
