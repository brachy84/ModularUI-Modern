package brachy.modularui.drawable;

import brachy.modularui.animation.IAnimatable;
import brachy.modularui.api.IJsonSerializable;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.serialization.json.JsonHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class Circle implements IDrawable, IJsonSerializable<Circle>, IAnimatable<Circle> {

    @Setter
    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    public Circle setColorInner(int colorInner) {
        return colorInner(colorInner);
    }

    public Circle setColorOuter(int colorOuter) {
        return colorOuter(colorOuter);
    }

    public Circle setColor(int inner, int outer) {
        return color(inner, outer);
    }

    public Circle setSegments(int segments) {
        return segments(segments);
    }

    public Circle color(int inner, int outer) {
        this.colorInner = inner;
        this.colorOuter = outer;
        return this;
    }

    public Circle color(int color) {
        return color(color, color);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        GuiDraw.drawEllipse(context.getGraphics(), x0, y0, width, height,
                this.colorInner, this.colorOuter, this.segments);
    }

    @Override
    public void loadFromJson(JsonObject json) {
        this.colorInner = JsonHelper.getColor(json, Color.WHITE.main, "colorInner", "color");
        this.colorOuter = JsonHelper.getColor(json, Color.WHITE.main, "colorOuter", "color");
        this.segments = JsonHelper.getInt(json, 40, "segments");
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("colorInner", this.colorInner);
        json.addProperty("colorOuter", this.colorOuter);
        json.addProperty("segments", this.segments);
        return true;
    }

    @Override
    public Circle interpolate(Circle start, Circle end, float t) {
        this.colorInner = Color.lerp(start.colorInner, end.colorInner, t);
        this.colorOuter = Color.lerp(start.colorOuter, end.colorOuter, t);
        this.segments = Interpolations.lerp(start.segments, end.segments, t);
        return this;
    }

    @Override
    public Circle copyOrImmutable() {
        return new Circle()
                .setColor(this.colorInner, this.colorOuter)
                .setSegments(this.segments);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Circle circle)) return false;

        return colorInner == circle.colorInner && colorOuter == circle.colorOuter && segments == circle.segments;
    }

    @Override
    public int hashCode() {
        int result = colorInner;
        result = 31 * result + colorOuter;
        result = 31 * result + segments;
        return result;
    }
}
