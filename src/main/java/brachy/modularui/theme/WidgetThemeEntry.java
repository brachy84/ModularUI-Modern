package brachy.modularui.theme;

import brachy.modularui.api.IThemeApi;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;

import java.util.List;
import java.util.stream.Stream;

public record WidgetThemeEntry<T extends WidgetTheme>(WidgetThemeKey<T> key, T theme, T hoverTheme) {

    public WidgetThemeEntry(WidgetThemeKey<T> key, T theme) {
        this(key, theme, theme);
    }

    public T getTheme(boolean hover) {
        return hover ? hoverTheme : theme;
    }

    @SuppressWarnings("unchecked")
    public <F extends WidgetTheme> WidgetThemeEntry<F> expectType(Class<F> expectedType) {
        if (this.key.isOfType(expectedType)) {
            return (WidgetThemeEntry<F>) this;
        }
        throw new IllegalStateException(
                String.format("Got widget theme with invalid type. Got type '%s', but expected type '%s'",
                        this.key.getWidgetThemeType().getSimpleName(), expectedType.getSimpleName()));
    }

    public <J> J encodeFallback(DynamicOps<J> ops, J prefix, List<String> errors) {
        var d = key().getCodec().encode(theme(), ops, prefix);
        var res = d.result();
        if (res.isEmpty()) {
            errors.add(d.error().orElseThrow().message());
        } else {
            prefix = res.get();
        }
        if (theme() == hoverTheme()) return prefix;
        d = key().getCodec().encode(hoverTheme(), ops, prefix);
        res = d.result();
        if (res.isEmpty()) {
            errors.add(d.error().orElseThrow().message());
            return prefix;
        }
        return res.get();
    }

    public <J> void encode(DynamicOps<J> ops, Stream.Builder<Pair<J, J>> mapBuilder, List<String> errors) {
        var d = key.getCodec().encodeStart(ops, theme);
        var res = d.result();
        if (res.isEmpty()) {
            errors.add(d.error().orElseThrow().message());
        } else {
            mapBuilder.accept(new Pair<>(ops.createString(key.getFullName()), res.get()));
        }
        if (theme == hoverTheme) return;
        d = key.getCodec().encodeStart(ops, theme);
        res = d.result();
        if (res.isEmpty()) {
            errors.add(d.error().orElseThrow().message());
            return;
        }
        mapBuilder.accept(new Pair<>(ops.createString(key.getFullName() + IThemeApi.HOVER_SUFFIX), res.get()));
    }
}
