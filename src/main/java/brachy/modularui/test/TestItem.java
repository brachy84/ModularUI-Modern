package brachy.modularui.test;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class TestItem extends Item implements IUIHolder<PlayerInventoryGuiData<?>> {

    public TestItem(Properties properties) {
        super(properties);
    }

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new ModularScreen(ModularUI.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        var cap = data.getUsedItemStack().getCapability(ITEM_HANDLER);
        if (!cap.isPresent() || cap.resolve().isEmpty()) return null;
        IItemHandler itemHandler = cap.resolve().get();
        syncManager.registerSlotGroup("mixer_items", 2);
        if (!(itemHandler instanceof IItemHandlerModifiable ihm)) return null;

        ModularPanel<?> panel = ModularPanel.defaultPanel("knapping_gui").resizeableOnDrag(true);
        panel.child(Flow.col().margin(7)
                        .child(new ParentWidget<>().widthRel(1f).expanded()
                                .child(SlotGroupWidget.builder()
                                        .row("I I")
                                        .row("  I")
                                        .row("   ")
                                        .row(" I ")
                                        .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(ihm, index)
                                                .ignoreMaxStackSize(true)
                                                .slotGroup("mixer_items")
                                                // do not allow putting items which can hold other items into the item
                                                // some mods don't do this on their backpacks, so it won't catch those cases
                                                .filter(stack -> !stack.getCapability(ITEM_HANDLER).isPresent())))
                                        .build()))
                        .child(SlotGroupWidget.playerInventory(false)))
                .child(GuiTextures.ANIMATED_TEXTURE_TEST.asWidget().size(32).leftRel(1f).topRel(0f).margin(7));

        return panel;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ITEM_HANDLER) {
                    var handler = new ItemStackHandler(4);
                    return LazyOptional.of(() -> handler).cast();
                }
                return LazyOptional.empty();
            }
        };
    }
}
