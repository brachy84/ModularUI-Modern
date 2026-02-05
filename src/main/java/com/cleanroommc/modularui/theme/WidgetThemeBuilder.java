package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.utils.serialization.json.JsonBuilder;

public class WidgetThemeBuilder<T extends WidgetTheme, B extends WidgetThemeBuilder<T, B>> extends JsonBuilder {

    @SuppressWarnings("unchecked")
    protected B getThis() {
        return (B) this;
    }

    public B defaultWidth(int defaultWidth) {
        add(IThemeApi.DEFAULT_WIDTH, defaultWidth);
        return getThis();
    }

    public B defaultHeight(int defaultHeight) {
        add(IThemeApi.DEFAULT_HEIGHT, defaultHeight);
        return getThis();
    }

    public B color(int color) {
        add(IThemeApi.COLOR, color);
        return getThis();
    }

    public B textShadow(int shadow) {
        add(IThemeApi.TEXT_SHADOW, shadow);
        return getThis();
    }

    public B iconColor(int color) {
        add(IThemeApi.ICON_COLOR, color);
        return getThis();
    }

    public B background(JsonBuilder background) {
        add(IThemeApi.BACKGROUND, background);
        return getThis();
    }

    public B background(IDrawable background) {
        add(IThemeApi.BACKGROUND, DrawableSerialization.serialize(background));
        return getThis();
    }

    public B background(String textureId) {
        return background(new JsonBuilder().add("type", "texture").add("id", textureId));
    }
}
