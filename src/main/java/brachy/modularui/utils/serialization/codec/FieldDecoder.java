package brachy.modularui.utils.serialization.codec;

public interface FieldDecoder<T, V> {

    V decodeField(T holder);
}
