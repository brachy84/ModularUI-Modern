package brachy.modularui.drawable.progress;

import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import brachy.modularui.utils.math.MathUtils;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import lombok.Getter;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * A progress texture which translates the progress into a circular angle. This works with any {@link brachy.modularui.api.drawable.IDrawable}.
 */
public class CircularProgressDrawable extends AbstractProgressDrawable<CircularProgressDrawable> {

    @Getter private Direction direction = Direction.CW;

    @Override
    protected void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        float p = progress;
        float angle = p * MathUtils.PI2;
        context.getStencil().push(() -> {
            ShaderInstance lastShader = RenderSystem.getShader();
            RenderSystem.setShader(GameRenderer::getPositionShader);
            Matrix4f pose = context.graphicsPose().last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            float wHalf = width / 2f, hHalf = height / 2f;
            float xc = x + wHalf, yc = y + hHalf;
            bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(pose, xc, yc, 0).endVertex(); // center

            if (this.direction == Direction.CW) {
                if (p > 7 / 8f) {
                    float tan = (float) Math.tan(angle - MathUtils.PI2);
                    bufferbuilder.vertex(pose, x + wHalf + tan * wHalf, y, 0).endVertex();
                    bufferbuilder.vertex(pose, x, y, 0).endVertex(); // top left
                }
                if (p > 5 / 8f) {
                    if (p < 7 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI - MathUtils.PI_HALF);
                        bufferbuilder.vertex(pose, x, y + hHalf - tan * hHalf, 0).endVertex();
                    }
                    bufferbuilder.vertex(pose, x, y + height, 0).endVertex(); // bottom left
                }
                if (p > 3 / 8f) {
                    if (p < 5 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI);
                        bufferbuilder.vertex(pose, x + wHalf - tan * wHalf, y + width, 0).endVertex();
                    }
                    bufferbuilder.vertex(pose, x + width, y + height, 0).endVertex(); // bottom right
                }
                if (p > 1 / 8f) {
                    if (p < 3 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI_HALF);
                        bufferbuilder.vertex(pose, x + width, y + hHalf + tan * hHalf, 0).endVertex();
                    }
                    bufferbuilder.vertex(pose, x + width, y, 0).endVertex(); // top right
                } else {
                    float tan = (float) Math.tan(angle);
                    bufferbuilder.vertex(pose, x + wHalf + tan * wHalf, y, 0).endVertex();
                }
                bufferbuilder.vertex(pose, xc, y, 0).endVertex(); // top center, starting angle
            } else {
                bufferbuilder.vertex(pose, xc, y, 0).endVertex(); // top center, starting angle
                if (p < 1 / 8f) {
                    float tan = (float) Math.tan(angle);
                    bufferbuilder.vertex(pose, x + wHalf - tan * wHalf, y, 0).endVertex();
                }
                if (p > 1 / 8f) {
                    bufferbuilder.vertex(pose, x, y, 0).endVertex(); // top left
                    if (p < 3 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI - MathUtils.PI_HALF);
                        bufferbuilder.vertex(pose, x, y + hHalf + tan * hHalf, 0).endVertex();
                    }
                }
                if (p > 3 / 8f) {
                    bufferbuilder.vertex(pose, x, y + height, 0).endVertex(); // bottom left
                    if (p < 5 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI);
                        bufferbuilder.vertex(pose, x + wHalf + tan * wHalf, y + width, 0).endVertex();
                    }
                }
                if (p > 5 / 8f) {
                    bufferbuilder.vertex(pose, x + width, y + height, 0).endVertex(); // bottom right
                    if (p < 7 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI_HALF);
                        bufferbuilder.vertex(pose, x + width, y + hHalf - tan * hHalf, 0).endVertex();
                    }
                }
                if (p > 7 / 8f) {
                    bufferbuilder.vertex(pose, x + width, y, 0).endVertex(); // top right
                    float tan = (float) Math.tan(angle);
                    bufferbuilder.vertex(pose, x + wHalf - tan * wHalf, y, 0).endVertex();
                }
            }

            tesselator.end();
            RenderSystem.setShader(() -> lastShader);

        }, x, y, width, height);
    }

    public CircularProgressDrawable direction(@Nullable Direction direction) {
        this.direction = direction == null ? Direction.CW : direction;
        return this;
    }

    public CircularProgressDrawable clockwise() {
        return direction(Direction.CW);
    }

    public CircularProgressDrawable counterClockwise() {
        return direction(Direction.CCW);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircularProgressDrawable that)) return false;
        if (!super.equals(o)) return false;

        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public enum Direction {
        CW, CCW
    }
}
