package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.codec.CodecUtil;

import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A stack of {@link IDrawable} backed by an array which are drawn on top of each other.
 */
public record DrawableStack(@Nullable IDrawable... drawables) implements IDrawable {

    public static final Codec<IDrawable> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            CodecUtil.checkedEncoder(IDrawable.CODEC.listOf().xmap(DrawableStack::fromList, DrawableStack::toList),
                    d -> d instanceof DrawableStack));

    public static final IDrawable[] EMPTY_BACKGROUND = {};
    public static final DrawableStack EMPTY = new DrawableStack(EMPTY_BACKGROUND);

    public static @Nullable IDrawable fromList(List<IDrawable> list) {
        return IDrawable.of(list.toArray(IDrawable[]::new));
    }

    public static List<IDrawable> toList(IDrawable drawable) {
        if (drawable instanceof DrawableStack stack) {
            return Arrays.stream(stack.drawables).filter(Objects::nonNull).toList();
        }
        return Collections.singletonList(drawable);
    }

    public DrawableStack(@Nullable IDrawable @Nullable... drawables) {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DrawableStack that)) return false;

        return Arrays.equals(drawables, that.drawables);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(drawables);
    }
}
