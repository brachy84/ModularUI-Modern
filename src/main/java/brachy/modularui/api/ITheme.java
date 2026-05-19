package brachy.modularui.api;

import brachy.modularui.theme.SelectableTheme;
import brachy.modularui.theme.SlotTheme;
import brachy.modularui.theme.TextFieldTheme;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.theme.WidgetThemeKey;
import brachy.modularui.utils.serialization.codec.CodecUtil;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A theme is parsed from JSON and contains style information like color or background texture.
 */
public interface ITheme {

    Encoder<ITheme> ENCODER = new Encoder<>() {
        @Override
        public <T> DataResult<T> encode(ITheme input, DynamicOps<T> ops, T prefix) {
            prefix = ops.set(prefix, "id", ops.createString(input.getId()));
            prefix = ops.set(prefix, "parent", ops.createString(input.getParentTheme().getId()));
            List<String> errors = new ArrayList<>();
            prefix = input.getFallback().encodeFallback(ops, prefix, errors);
            var mapBuilder = CodecUtil.mergePrefixToMapBuilder(ops, prefix);
            Map<String, Set<WidgetTheme>> encodedThemes = new Object2ObjectOpenHashMap<>();
            for (WidgetThemeEntry<?> entry : input.getWidgetThemes()) {
                if (entry.key() == IThemeApi.FALLBACK) continue;
                var set = encodedThemes.computeIfAbsent(entry.key().getName(), k -> new ObjectOpenHashSet<>());
                if (entry.key().isSubWidgetTheme() && set.contains(entry.theme()) && set.contains(entry.hoverTheme())) continue;
                entry.encode(ops, mapBuilder, errors);
                set.add(entry.theme());
                set.add(entry.hoverTheme());
            }
            if (!errors.isEmpty()) {
                return DataResult.error(() -> String.format("Error while encoding theme '%s': %s", input.getId(), errors));
            }
            return DataResult.success(ops.createMap(mapBuilder.build()));
        }
    };

    /**
     * @return the master default theme.
     */
    static ITheme getDefault() {
        return IThemeApi.get().getDefaultTheme();
    }

    /**
     * @param id theme id
     * @return theme with given id
     */
    static ITheme get(String id) {
        return IThemeApi.get().getTheme(id);
    }

    /**
     * @return theme id
     */
    String getId();

    /**
     * @return parent theme
     */
    ITheme getParentTheme();

    @UnmodifiableView
    Collection<WidgetThemeEntry<?>> getWidgetThemes();

    WidgetThemeEntry<WidgetTheme> getFallback();

    WidgetThemeEntry<WidgetTheme> getPanelTheme();

    WidgetThemeEntry<WidgetTheme> getButtonTheme();

    WidgetThemeEntry<WidgetTheme> getScrollbarTheme();

    WidgetThemeEntry<SlotTheme> getItemSlotTheme();

    WidgetThemeEntry<SlotTheme> getFluidSlotTheme();

    WidgetThemeEntry<TextFieldTheme> getTextFieldTheme();

    WidgetThemeEntry<SelectableTheme> getToggleButtonTheme();

    <T extends WidgetTheme> WidgetThemeEntry<T> getWidgetTheme(WidgetThemeKey<T> key);
}
