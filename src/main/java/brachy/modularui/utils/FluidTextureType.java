package brachy.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;

public enum FluidTextureType {

    STILL((fluidTypeExtensions, fluidStack) -> {
        if (!fluidStack.isEmpty()) return fluidTypeExtensions.getStillTexture(fluidStack);
        else return fluidTypeExtensions.getStillTexture();
    }),
    FLOWING((fluidTypeExtensions, fluidStack) -> {
        if (!fluidStack.isEmpty()) return fluidTypeExtensions.getFlowingTexture(fluidStack);
        else return fluidTypeExtensions.getFlowingTexture();
    }),
    OVERLAY((fluidTypeExtensions, fluidStack) -> {
        if (!fluidStack.isEmpty()) return fluidTypeExtensions.getOverlayTexture(fluidStack);
        else return fluidTypeExtensions.getOverlayTexture();
    });

    private static final ResourceLocation WATER_STILL = new ResourceLocation("minecraft", "block/water_still");

    private final BiFunction<IClientFluidTypeExtensions, FluidStack, @Nullable ResourceLocation> mapper;

    FluidTextureType(BiFunction<IClientFluidTypeExtensions, FluidStack, @Nullable ResourceLocation> mapper) {
        this.mapper = mapper;
    }

    public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions) {
        return map(fluidTypeExtensions, FluidStack.EMPTY);
    }

    public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions, FluidStack fluidStack) {
        ResourceLocation texture = mapper.apply(fluidTypeExtensions, fluidStack);
        if (texture == null) texture = STILL.mapper.apply(fluidTypeExtensions, fluidStack);
        if (texture == null) texture = WATER_STILL;

        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }
}
