package com.cleanroommc.modularui.test;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.cleanroommc.modularui.ModularUI;

public class ModularUIItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ModularUI.MOD_ID);

    public static final RegistryObject<TestItem> TEST_ITEM = ITEMS.register("mui_test_item", () -> new TestItem(new Item.Properties()));

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
