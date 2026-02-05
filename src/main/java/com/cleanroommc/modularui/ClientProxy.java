package com.cleanroommc.modularui;

import com.cleanroommc.modularui.animation.AnimatorManager;
import com.cleanroommc.modularui.client.CursorHandler;
import com.cleanroommc.modularui.drawable.DrawableSerialization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;

public class ClientProxy extends CommonProxy {

    @Getter
    private static final Timer timer60Fps = new Timer(60f, 0);

    public ClientProxy() {
        if (!ModularUI.isDataGen()) {
            CursorHandler.init();
            AnimatorManager.init();
            // enable stencil bits, must call on render thread
            RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());

            DrawableSerialization.init();
        }
    }
}
