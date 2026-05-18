package brachy.modularui.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IJsonSerializable;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.json.JsonHelper;

import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A stack of {@link IDrawable} backed by an array which are drawn on top of each other.
 */
public record DrawableStack(IDrawable... drawables) implements IDrawable, IJsonSerializable<DrawableStack> {

    public static final Codec<IDrawable> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            CodecUtil.checkedEncoder(IDrawable.CODEC.listOf().xmap(DrawableStack::fromList, DrawableStack::toList),
                    d -> d instanceof DrawableStack));

    public static final IDrawable[] EMPTY_BACKGROUND = {};
    public static final DrawableStack EMPTY = new DrawableStack(EMPTY_BACKGROUND);

    public static IDrawable fromList(List<IDrawable> list) {
        return IDrawable.of(list.toArray(IDrawable[]::new));
    }

    public static List<IDrawable> toList(IDrawable drawable) {
        if (drawable instanceof DrawableStack stack) {
            return List.of(stack.drawables);
        }
        return Collections.singletonList(drawable);
    }

    public DrawableStack(IDrawable... drawables) {
        this.drawables = drawables == null || drawables.length == 0 ? EMPTY_BACKGROUND : drawables;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null) drawable.draw(context, x, y, width, height, widgetTheme);
        }
    }

    @Override
    public boolean canApplyTheme() {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null && drawable.canApplyTheme()) {
                return true;
            }
        }
        return false;
    }

    public static IDrawable parseJson(JsonObject json) {
        JsonElement drawables = JsonHelper.getJsonElement(json, "drawables", "children");
        if (drawables != null && drawables.isJsonArray()) {
            return parseJson(drawables.getAsJsonArray());
        }
        ModularUI.LOGGER.throwing(
                new JsonParseException("DrawableStack json should have an array named 'drawables' or 'children'."));
        return IDrawable.EMPTY;
    }

    public static IDrawable parseJson(JsonArray drawables) {
        List<IDrawable> list = new ArrayList<>();
        for (JsonElement child : drawables) {
            IDrawable drawable = JsonHelper.deserialize(child, IDrawable.class);
            if (drawable != null) {
                list.add(drawable);
            }
        }
        if (list.isEmpty()) {
            return IDrawable.EMPTY;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return new DrawableStack(list.toArray(IDrawable[]::new));
    }

    // this method should never be called, but the special casing code is copied here in case it does.
    @Override
    public boolean saveToJson(JsonObject json) {
        JsonArray jsonArray = new JsonArray();
        for (IDrawable drawable : this.drawables()) {
            jsonArray.add(JsonHelper.serialize(drawable));
        }
        json.add("drawables", jsonArray);
        return true;
    }
}
