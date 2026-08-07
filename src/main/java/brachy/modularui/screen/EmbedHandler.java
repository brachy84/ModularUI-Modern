package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Matrix4f;

import java.util.function.Predicate;

public class EmbedHandler {

    public static int getEmbedWidth(ModularScreen screen) {
        return screen.getMainPanel().getArea().width;
    }

    public static int getEmbedHeight(ModularScreen screen) {
        return screen.getMainPanel().getArea().height;
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphics graphics, float partialTicks) {
        drawEmbed(screen, graphics, partialTicks, r -> true);
    }

    public static void drawEmbedNoVanillaElements(ModularScreen screen, GuiGraphics graphics, float partialTicks) {
        drawEmbed(screen, graphics, partialTicks, r -> false);
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphics graphics, float partialTicks, Predicate<Renderable> vanillaElementFilter) {
        screen.getContext().reset();
        PoseStack pose = graphics.pose();
        var m = pose.last().pose();
        screen.updateEmbedPos(Math.round(m.m30()), Math.round(m.m31()));
        Matrix4f hostTransform = new Matrix4f().translation(-Math.round(m.m30()), -Math.round(m.m31()), 0f).mul(m);
        screen.getMainPanel().transform((p, stack) -> stack.multiply(hostTransform));
        pose.pushPose();
        pose.setIdentity();

        var defContext = ClientScreenHandler.getDefaultContext();
        int mx = defContext.getAbsMouseX();
        int my = defContext.getAbsMouseY();
        screen.render(graphics, mx, my, partialTicks);

        if (vanillaElementFilter != null) {
            RenderSystem.disableDepthTest();
            ClientScreenHandler.drawVanillaElements(graphics, screen.getScreenWrapper().wrappedScreen(), mx, my, partialTicks, vanillaElementFilter);
        }

        screen.drawForeground(graphics);

        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        pose.popPose();
    }

    public record EmbedWrapper(ModularScreen screen) implements IMuiScreen {

        @Override
        public Screen wrappedScreen() {
            return Minecraft.getInstance().screen;
        }

        @Override
        public void updateGuiArea(Rectangle area) {}
    }
}
