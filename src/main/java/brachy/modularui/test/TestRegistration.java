package brachy.modularui.test;

import brachy.modularui.ModularUI;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class TestRegistration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModularUI.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModularUI.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModularUI.MOD_ID);

    public static final RegistryObject<Block> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(TestBlockEntity::new));
    public static final RegistryObject<Item> TEST_BLOCK_ITEM = ITEMS.register("test_block", () -> new BlockItem(TEST_BLOCK.get(), new Item.Properties()));

    private static Supplier<TestItem> testItemFactory() {
        if (ModularUI.Mods.CURIOS.isLoaded()) {
            return () -> new TestCurioItem(new Item.Properties());
        }
        return () -> new TestItem(new Item.Properties());
    }

    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item", testItemFactory());
    public static final RegistryObject<BlockEntityType<?>> BE_TYPE = BE_TYPES.register("test_block", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST_BLOCK.get()).build(null));

    public static final RegistryObject<Block> TEST_MACHINE_BLOCK = BLOCKS.register("machine_block", () -> new TestBlock(TestMachine.BE::new));
    public static final RegistryObject<Item> TEST_MACHINE_BLOCK_ITEM = ITEMS.register("machine_block", () -> new BlockItem(TEST_MACHINE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<?>> MACHINE_BE_TYPE = BE_TYPES.register("machine_block", () -> BlockEntityType.Builder.of(TestMachine.BE::new, TEST_MACHINE_BLOCK.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE_TYPES.register(bus);
    }
}
