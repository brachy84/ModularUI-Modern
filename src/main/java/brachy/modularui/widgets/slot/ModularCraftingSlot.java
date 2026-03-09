package brachy.modularui.widgets.slot;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import static net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent;

/**
 * Basically a copy of {@link net.minecraft.world.inventory.ResultSlot} for {@link ModularSlot}.
 */
@SuppressWarnings("unused")
public class ModularCraftingSlot extends ModularSlot {

    private @Nullable CraftingContainerWrapper craftSlots = null;
    private int gridWidth = 3, gridHeight = 3;
    private int gridStartIndex = 0;

    private int amountCrafted;

    private final Consumer<Slot> slotChangeListener = this::updateCraftResult;

    public ModularCraftingSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);

        this.canPut(false);
    }

    public ModularCraftingSlot gridWidth(int width) {
        this.gridWidth = width;
        // reset craftSlots in case this slot is modified after the screen is built
        this.craftSlots = null;
        return this;
    }

    public ModularCraftingSlot gridHeight(int height) {
        this.gridHeight = height;
        // reset craftSlots in case this slot is modified after the screen is built
        this.craftSlots = null;
        return this;
    }

    public ModularCraftingSlot grid(int width, int height) {
        this.gridWidth = width;
        this.gridHeight = height;
        // reset craftSlots in case this slot is modified after the screen is built
        this.craftSlots = null;
        return this;
    }

    public ModularCraftingSlot grid(int width, int height, int startIndex) {
        this.gridWidth = width;
        this.gridHeight = height;
        return this.gridStartIndex(startIndex);
    }

    public ModularCraftingSlot gridStartIndex(int startIndex) {
        this.gridStartIndex = startIndex;
        // reset craftSlots in case this slot is modified after the screen is built
        this.craftSlots = null;
        return this;
    }

    public CraftingContainerWrapper getCraftSlots() {
        if (this.craftSlots == null) {
            this.craftSlots = new CraftingContainerWrapper(this,
                    this.gridWidth, this.gridHeight,
                    (IItemHandlerModifiable) this.getItemHandler(), this.gridStartIndex);
            this.craftSlots.notifyContainer();
        }
        return this.craftSlots;
    }

    // Register the slot change listener
    @Override
    public ModularSlot slotGroup(SlotGroup slotGroup) {
        if (this.getSlotGroup() == slotGroup) return this;
        if (this.getSlotGroup() != null) {
            this.getSlotGroup().removeSlotChangeListener(this.slotChangeListener);
        }
        if (slotGroup != null) {
            slotGroup.addSlotChangeListener(this.slotChangeListener);
        }

        return super.slotGroup(slotGroup);
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public @NotNull ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(amount, this.getItem().getCount());
        }

        return super.remove(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        this.amountCrafted += numItemsCrafted;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    protected void checkTakeAchievements(@NotNull ItemStack stack) {
        if (this.amountCrafted > 0) {
            stack.onCraftedBy(getPlayer().level(), getPlayer(), this.amountCrafted);
            net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(getPlayer(), stack, this.craftSlots);
        }

        this.amountCrafted = 0;

        if (this.container instanceof RecipeHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(getPlayer(), this.craftSlots.getItems());
        }
        if (this.getItemHandler() instanceof RecipeHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(getPlayer(), this.craftSlots.getItems());
        }
    }

    @Override
    public void onCraftShiftClick(Player playerIn, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            playerIn.drop(itemStack, false);
        }
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        this.checkTakeAchievements(stack);
        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> nonnulllist = player.level().getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING,
                this.craftSlots, player.level());
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = this.craftSlots.getItem(i);
            ItemStack itemstack1 = nonnulllist.get(i);

            if (!itemstack.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                itemstack = this.craftSlots.getItem(i);
            }

            if (!itemstack1.isEmpty()) {
                if (itemstack.isEmpty()) {
                    this.craftSlots.setItem(i, itemstack1);
                } else if (ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
                    itemstack1.grow(itemstack.getCount());
                    this.craftSlots.setItem(i, itemstack1);
                } else if (!getPlayer().getInventory().add(itemstack1)) {
                    getPlayer().drop(itemstack1, false);
                }
            }
        }
        craftMatrix.notifyContainer();
    }

    protected void updateCraftResult(Slot slot) {
        // don't check possible crafting recipes if this is the slot that changed
        if (slot == this) {
            return;
        }
        // acts as a side check and a cast
        if (!(this.getSyncHandler().getSyncManager().getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        Level level = player.level();
        ItemStack result = ItemStack.EMPTY;

        Optional<CraftingRecipe> possibleRecipe = player.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, this.getCraftSlots(), level);
        if (possibleRecipe.isEmpty()) {
            return;
        }
        CraftingRecipe recipe = possibleRecipe.get();

        if (setRecipeUsed(this.getItemHandler(), player, recipe)) {
            result = recipe.assemble(this.getCraftSlots(), level.registryAccess());
            if (!result.isItemEnabled(level.enabledFeatures())) {
                return;
            }
        }

        set(result);
        getSyncHandler().forceSyncItem();
    }

    protected boolean setRecipeUsed(@Nullable Object possibleRecipeHolder, ServerPlayer player, Recipe<?> recipe) {
        if (!recipe.isSpecial() &&
                player.level().getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !player.getRecipeBook().contains(recipe)) {
            return false;
        }
        if (possibleRecipeHolder instanceof RecipeHolder recipeHolder) {
            recipeHolder.setRecipeUsed(recipe);
        }
        return true;
    }
}
