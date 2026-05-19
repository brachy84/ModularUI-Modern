package brachy.modularui.theme;

import brachy.modularui.utils.serialization.codec.FieldReader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record WidgetThemeField<T extends WidgetTheme, V>(String name, Class<V> type, Codec<V> codec, FieldReader<T, V> fieldReader,
                                                         boolean canFallback) {

    <J> void encode(T widgetTheme, DynamicOps<J> ops, Stream.Builder<Pair<J, J>> mapBuilder, List<String> errors) {
        var value = fieldReader().readField(widgetTheme);
        J data;
        if (value == null) {
            data = ops.empty();
        } else {
            var d = codec().encodeStart(ops, value);
            var res = d.result();
            if (res.isEmpty()) {
                errors.add(d.error().orElseThrow().message());
                return;
            }
            data = res.get();
        }
        mapBuilder.accept(new Pair<>(ops.createString(name()), data));
    }

    <J> V decode(DynamicOps<J> ops, Map<String, J> map, List<String> errors) {
        J element = map.get(name());
        if (element == null) {
            if (!map.containsKey(name())) {
                errors.add(String.format("Field '%s' in widget theme of type %s was not found", name(), type().getSimpleName()));
            }
            return null;
        }
        var d = codec().parse(ops, element);
        var res = d.result();
        if (res.isEmpty()) {
            errors.add(d.error().orElseThrow().message());
            return null;
        }
        return res.get();
    }
}
