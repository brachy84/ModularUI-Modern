package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.TextWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.IntSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class StyledText extends BaseKey {

    private final IKey key;
    @Getter
    @Setter
    private Alignment alignment = Alignment.Center;
    @Getter
    @Setter
    private IntSupplier color = null;
    @Setter
    private @Nullable Boolean shadow = null;
    @Getter
    @Setter
    private float scale = 1f;

    public StyledText(IKey key) {
        this.key = key;
    }

    @Override
    public MutableComponent get() {
        return this.key.get();
    }

    @Override
    public MutableComponent getFormatted() {
        return this.key.getFormatted();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        renderer.setAlignment(this.alignment, width, height);
        renderer.setColor(this.color != null ? this.color.getAsInt() : widgetTheme.getTextColor());
        renderer.setScale(this.scale);
        renderer.setPos(x, y);
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.isTextShadow());
        renderer.draw(context.getGraphics(), getFormatted());
    }

    public @Nullable Boolean isShadow() {
        return this.shadow;
    }

    @Override
    public @NotNull BaseKey style(ChatFormatting formatting) {
        this.key.style(formatting);
        return this;
    }

    @Override
    public TextWidget<?> asWidget() {
        return new TextWidget<>(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }

    @Override
    public AnimatedText withAnimation() {
        return new AnimatedText(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }
}
