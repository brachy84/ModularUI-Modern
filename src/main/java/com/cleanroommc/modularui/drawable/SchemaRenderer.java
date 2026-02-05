package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.schema.ISchema;
import com.cleanroommc.modularui.client.schemarenderer.BaseSchemaRenderer;
import com.cleanroommc.modularui.client.schemarenderer.BlockHighlight;
import com.cleanroommc.modularui.client.schemarenderer.Camera;

import net.minecraft.world.phys.BlockHitResult;
import com.mojang.blaze3d.vertex.PoseStack;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class SchemaRenderer extends BaseSchemaRenderer {

    @Setter
    protected DoubleSupplier scale;
    @Setter
    protected BooleanSupplier disableBER;
    @Setter
    protected Consumer<SchemaRenderer> afterRender;
    @Setter
    protected BiConsumer<Camera, ISchema> cameraFunc;
    @Setter
    protected Supplier<BlockHighlight> highlight;
    @Setter
    protected boolean isometric = false;
    @Setter
    private boolean rayTracing = false;

    public SchemaRenderer(ISchema schema) {
        super(schema);
    }

    @Tolerate
    public SchemaRenderer scale(double scale) {
        this.scale = () -> scale;
        return this;
    }

    @Tolerate
    public SchemaRenderer disableBER(boolean disableBER) {
        this.disableBER = () -> disableBER;
        return this;
    }

    public SchemaRenderer highlightRenderer(BlockHighlight highlight) {
        this.highlight = () -> highlight;
        this.rayTracing = true;
        return this;
    }

    @Override
    protected void onSetupCamera() {
        if (this.scale != null) {
            camera().scaleDistanceKeepLookAt((float) this.scale.getAsDouble());
        }
        if (this.cameraFunc != null) {
            this.cameraFunc.accept(camera(), schema());
        }
    }

    @Override
    protected void onRendered() {
        if (this.afterRender != null) {
            this.afterRender.accept(this);
        }
    }

    @Override
    protected void onSuccessfulRayTrace(PoseStack poseStack, @NotNull BlockHitResult result) {
        if (this.highlight != null) {
            this.highlight.get().renderHighlight(poseStack, result, camera().pos());
        }
    }

    @Override
    public boolean doRayTrace() {
        return this.rayTracing;
    }

    @Override
    public boolean isBEREnabled() {
        return this.disableBER == null || !this.disableBER.getAsBoolean();
    }

    @Override
    public boolean isIsometric() {
        return isometric;
    }
}
