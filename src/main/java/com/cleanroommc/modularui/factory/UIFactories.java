package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IUIHolder;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

public class UIFactories {

    public static BlockEntityUIFactory blockEntity() {
        return BlockEntityUIFactory.INSTANCE;
    }

    public static SidedBlockEntityUIFactory sidedBlockEntity() {
        return SidedBlockEntityUIFactory.INSTANCE;
    }

    public static EntityUIFactory entity() {
        return EntityUIFactory.INSTANCE;
    }

    public static PlayerInventoryUIFactory playerInventory() {
        return PlayerInventoryUIFactory.INSTANCE;
    }

    public static SimpleUIFactory createSimple(ResourceLocation name, IUIHolder<GuiData> holder) {
        return new SimpleUIFactory(name, holder);
    }

    public static SimpleUIFactory createSimple(ResourceLocation name, Supplier<IUIHolder<GuiData>> holder) {
        return new SimpleUIFactory(name, holder);
    }

    @ApiStatus.Internal
    public static void init() {
        GuiManager.registerFactory(blockEntity());
        GuiManager.registerFactory(sidedBlockEntity());
        GuiManager.registerFactory(entity());
        GuiManager.registerFactory(playerInventory());
    }

    private UIFactories() {}
}
