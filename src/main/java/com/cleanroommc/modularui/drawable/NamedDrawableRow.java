package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;

import lombok.Getter;

import org.jetbrains.annotations.Nullable;

public class NamedDrawableRow implements IDrawable {

    @Getter
    private IKey name;
    @Getter
    private IIcon drawable;

    public NamedDrawableRow() {
        this(null, null);
    }

    public NamedDrawableRow(@Nullable IKey name, @Nullable IIcon drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.name != null) {
            this.name.drawAligned(context, x, y, width, height, widgetTheme, Alignment.CenterLeft);
        }
        if (this.drawable != null) {
            int wd = this.drawable.getWidth() + this.drawable.getMargin().horizontal();
            int xd = x + width - wd;
            this.drawable.draw(context, xd, y, wd, height, widgetTheme);
        }
    }

    @Override
    public int getDefaultWidth() {
        int w = 0;
        if (this.name != null) w += this.name.getDefaultWidth();
        if (this.drawable != null) w += this.drawable.getWidth();
        return w;
    }

    @Override
    public int getDefaultHeight() {
        int h = 0;
        if (this.name != null) h += this.name.getDefaultHeight();
        if (this.drawable != null) h += this.drawable.getHeight();
        return h;
    }

    public NamedDrawableRow name(@Nullable IKey key) {
        this.name = key;
        return this;
    }

    public NamedDrawableRow drawable(@Nullable IIcon drawable) {
        this.drawable = drawable;
        return this;
    }
}
