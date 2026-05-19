package brachy.modularui.theme;

import brachy.modularui.utils.serialization.codec.CodecUtil;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class WidgetThemeCodec<T extends WidgetTheme> implements Codec<T> {

    @Getter
    private final Class<T> type;
    @Getter(AccessLevel.PACKAGE)
    private final List<WidgetThemeField<T, ?>> fields;
    private final Constructor<T> ctor;

    public WidgetThemeCodec(Class<T> type, List<WidgetThemeField<T, ?>> fields, Constructor<T> ctor) {
        this.type = type;
        this.fields = fields;
        this.ctor = ctor;
    }

    @Override
    public <J> DataResult<Pair<T, J>> decode(DynamicOps<J> ops, J input) {
        var d = ops.getMapValues(input);
        var res = d.result();
        if (res.isEmpty()) return DataResult.error(() -> d.error().orElseThrow().message());
        var map = new Object2ObjectOpenHashMap<String, J>();
        res.get().forEach(p -> map.put(ops.getStringValue(p.getFirst()).result().orElseThrow(), p.getSecond()));
        List<String> errors = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        this.fields.forEach(f -> args.add(f.decode(ops, map, errors)));
        if (!errors.isEmpty()) {
            return DataResult.error(() ->
                    String.format("Errors while decoding widget theme: %s", errors));
        }

        T instance = null;
        try {
            instance = this.ctor.newInstance(args.toArray(Object[]::new));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
        return DataResult.success(new Pair<>(instance, input));
    }

    @Override
    public <J> DataResult<J> encode(T input, DynamicOps<J> ops, J prefix) {
        var mapBuilder = CodecUtil.mergePrefixToMapBuilder(ops, prefix);
        List<String> errors = new ArrayList<>();
        this.fields.forEach(f -> f.encode(input, ops, mapBuilder, errors));
        if (!errors.isEmpty()) {
            return DataResult.error(() -> "Error while encoding widget theme of type " + input.getClass().getSimpleName() + ": " + errors);
        }
        return DataResult.success(ops.createMap(mapBuilder.build()));
    }
}
