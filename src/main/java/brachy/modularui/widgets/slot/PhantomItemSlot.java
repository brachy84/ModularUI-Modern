package brachy.modularui.widgets.slot;

import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.ItemSlotSyncHandler;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;

import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PhantomItemSlot extends ItemSlot implements GhostIngredientSlot<ItemStack> {

    private PhantomItemSlotSyncHandler syncHandler;

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncOrValue(@NonNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof PhantomItemSlotSyncHandler;
    }

    @Override
    protected void setSyncOrValue(@NonNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(PhantomItemSlotSyncHandler.class);
    }

    @Override
    protected void drawOverlay(ModularGuiContext context) {
        RecipeViewerHandler handler = RecipeViewerHandler.getCurrent();
        if (handler.isHoveringOver(this)) {
            drawHighlight(context, getArea(), isHovering());
        } else {
            super.drawOverlay(context);
        }
    }

    @Override
    public @NonNull Result onMousePressed(int button) {
        MouseData mouseData = MouseData.create(button);
        this.syncHandler.syncToServer(PhantomItemSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseReleased(int button) {
        return true;
    }

    @Override
    public boolean onMouseScrolled(double delta) {
        MouseData mouseData = MouseData.create((int) delta);
        this.syncHandler.syncToServer(PhantomItemSlotSyncHandler.SYNC_SCROLL, mouseData::writeToPacket);
        return true;
    }

    @Override
    public void onMouseDrag(int button, double dragX, double dragY) {
        // TODO custom drag impl
    }

    @Override
    public void setGhostIngredient(@NonNull ItemStack ingredient) {
        this.syncHandler.updateFromClient(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NonNull Object ingredient) {
        return areAncestorsEnabled() &&
                this.syncHandler.isPhantom() &&
                ingredient instanceof ItemStack itemStack &&
                this.syncHandler.isItemValid(itemStack) ? itemStack : null;
    }

    @Override
    @NonNull
    public PhantomItemSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    @Override
    public PhantomItemSlot slot(ModularSlot slot) {
        return syncHandler(new PhantomItemSlotSyncHandler(slot));
    }

    @Override
    public PhantomItemSlot syncHandler(ItemSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return false;
    }
}
