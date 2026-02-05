package com.cleanroommc.modularui.theme;

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
}
