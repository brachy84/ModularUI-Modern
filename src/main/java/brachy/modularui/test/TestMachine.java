package brachy.modularui.test;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IDoubleValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.SingleChildWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;

import dev.emi.emi.api.EmiRegistry;

import dev.emi.emi.api.recipe.EmiRecipeCategory;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import lombok.Getter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import net.minecraftforge.items.wrapper.EmptyHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TestMachine {

    private static final IItemHandler EMPTY_INFINITE_ITEM_HANDLER = new EmptyHandler() {
        @Override
        public int getSlots() {
            return Integer.MAX_VALUE; // pls don't iterate UwU
        }
    };

    public static class BE extends AbstractBlockEntity implements IUIHolder<PosGuiData> {

        private final ItemStackHandler input = new ItemStackHandler(4);
        private final ItemStackHandler output = new ItemStackHandler(4);
        private final LazyOptional<IItemHandlerModifiable> inv = LazyOptional.of(() -> new CombinedInvWrapper(input, output));

        private int ticks = 0;
        private boolean running = false, paused = false;
        private int recipeProgress = 0;
        @Nullable private Recipe lastRecipe = null;

        public BE(BlockPos pos, BlockState blockState) {
            super(TestRegistration.MACHINE_BE_TYPE.get(), pos, blockState);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                return this.inv.cast();
            }
            return super.getCapability(cap, side);
        }

        @Override
        public ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
            return new ModularScreen(ModularUI.MOD_ID, mainPanel);
        }

        @Override
        public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
            return new ModularPanel<>("machine")
                    .coverChildren()
                    .invisible()
                    .child(Flow.col()
                            .coverChildren()
                            .childPadding(2)
                            .child(new ParentWidget<>()
                                    .coverChildren(176, 30)
                                    .padding(7)
                                    .widgetTheme(IThemeApi.PANEL)
                                    .child(Recipes.buildMachineUI(this.input, this.output, new DoubleSyncValue(this::getProgress)))
                                    .child(new ParentWidget<>()
                                            .coverChildren()
                                            .decoration()
                                            .padding(4)
                                            .background(GuiTextures.MC_BACKGROUND.getSubArea(0, 0, 1, 0.5f))
                                            .horizontalCenter()
                                            .anchorTop(1)
                                            .child(Text.str("Machine Name").asWidget())
                                            .name("title"))
                                    .child(new ParentWidget<>()
                                            .coverChildren()
                                            .decoration()
                                            .padding(4)
                                            .paddingLeft(1)
                                            .background(GuiTextures.MC_BACKGROUND.getSubArea(0.5f, 0, 1, 1f))
                                            .bottom(7)
                                            .rightRelAnchor(0, 1f)
                                            .child(new ToggleButton()
                                                    .value(new BooleanSyncValue(() -> this.paused, v -> this.paused = v))
                                                    .overlay(true, GuiTextures.PLAY)
                                                    .overlay(false, GuiTextures.PAUSE))
                                            .name("side_options"))
                            )
                            .child(new SingleChildWidget<>()
                                    .coverChildren()
                                    .padding(7)
                                    .widgetTheme(IThemeApi.PANEL)
                                    .child(SlotGroupWidget.playerInventory(false))));
        }

        @Override
        public void update() {
            if (hasLevel() && !getLevel().isClientSide) {
                if (this.running && this.lastRecipe != null && !this.paused) {
                    if (++this.recipeProgress == this.lastRecipe.ticks) {
                        this.lastRecipe.finishRecipe(this.output);
                        this.recipeProgress = 0;
                        if (this.lastRecipe.startRecipe(this.input, true)) {
                            this.lastRecipe.startRecipe(this.input, false);
                        } else {
                            this.running = false;
                        }
                    }
                } else if (this.ticks % 20 == 0) {
                    Recipe recipe = Recipes.findRecipe(this.input, this.lastRecipe);
                    if (recipe != null) {
                        this.lastRecipe = recipe;
                        this.recipeProgress = 0;
                        this.lastRecipe.startRecipe(this.input, false);
                        this.running = true;
                    }
                }
            }
            this.ticks++;
        }

        public double getProgress() {
            if (this.lastRecipe == null || !this.running) return 0;
            return (double) this.recipeProgress / this.lastRecipe.ticks;
        }
    }

    public static class Recipe {

        private final ResourceLocation resloc;
        private final List<ItemStack> in = new ArrayList<>(), out = new ArrayList<>();
        private int ticks = 80;

        public Recipe(String resloc) {
            this.resloc = ModularUI.id(resloc);
        }

        public Recipe in(ItemStack stack) {
            this.in.add(stack);
            return this;
        }

        public Recipe in(Item item, int count) {
            return in(new ItemStack(item, count));
        }

        public Recipe in(Item item) {
            return in(item, 1);
        }

        public Recipe out(ItemStack stack) {
            this.out.add(stack);
            return this;
        }

        public Recipe out(Item item, int count) {
            return out(new ItemStack(item, count));
        }

        public Recipe out(Item item) {
            return out(item, 1);
        }

        public Recipe ticks(int ticks) {
            this.ticks = ticks;
            return this;
        }

        public boolean startRecipe(IItemHandler handler, boolean simulate) {
            for (ItemStack in : this.in) {
                if (!Recipes.extract(in, handler, simulate)) {
                    return false;
                }
            }
            return true;
        }

        public void finishRecipe(IItemHandler handler) {
            for (ItemStack out : this.out) {
                ItemHandlerHelper.insertItemStacked(handler, out, false);
            }
        }
    }

    public static class Recipes {

        public static final List<Recipe> list = new ArrayList<>();

        static {
            list.add(new Recipe("/stuff_to_nether_star")
                    .in(Items.DIAMOND)
                    .in(Items.EMERALD)
                    .in(Items.GOLD_INGOT, 4)
                    .out(Items.NETHER_STAR));
        }

        public static Recipe findRecipe(IItemHandler input, @Nullable Recipe lastRecipe) {
            if (lastRecipe != null && lastRecipe.startRecipe(input, true)) return lastRecipe;
            for (Recipe recipe : list) {
                if (recipe.startRecipe(input, true)) {
                    return recipe;
                }
            }
            return null;
        }

        private static boolean extract(ItemStack stack, IItemHandler handler, boolean simulate) {
            if (stack.isEmpty()) return true;
            int extracted = 0;
            for (int i = 0, n = handler.getSlots(); i < n; i++) {
                ItemStack c = handler.extractItem(i, stack.getCount() - extracted, true);
                if (ItemStack.isSameItemSameTags(stack, c)) {
                    if (!simulate) {
                        c = handler.extractItem(i, stack.getCount() - extracted, false);
                    }
                    extracted += c.getCount();
                    if (extracted >= stack.getCount()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static IWidget buildMachineUI(IItemHandler in, IItemHandler out, IDoubleValue<?> progress) {
            return Flow.row().name("slots")
                    .coverChildren()
                    .center()
                    .childPadding(8)
                    .child(SlotGroupWidget.rect(2, 2, i -> new ItemSlot()
                            .slot(new ModularSlot(in, i))
                            .recipeRole(RecipeSlotRole.INPUT)))
                    .child(new ProgressWidget()
                            .value(progress)
                            .size(20)
                            .texture(GuiTextures.PROGRESS_ARROW, 20))
                    .child(SlotGroupWidget.rect(2, 2, i -> new ItemSlot()
                            .slot(new ModularSlot(out, i).canPut(false))
                            .recipeRole(RecipeSlotRole.OUTPUT)));
        }

        public static IWidget buildViewerUI(Recipe recipe) {
            return Flow.row().name("slots")
                    .center()
                    .childPadding(8)
                    .child(SlotGroupWidget.rect(2, 2, i -> {
                        var in = i >= recipe.in.size() ? ItemStack.EMPTY : recipe.in.get(i);
                        if (in == null) in = ItemStack.EMPTY;
                        return RecipeViewerSlotWidget.create()
                                .recipeSlotRole(RecipeSlotRole.INPUT)
                                .value(in);
                    }))
                    .child(new ProgressWidget()
                            .value(DoubleValue.simulateProgress(5000))
                            .size(20)
                            .texture(GuiTextures.PROGRESS_ARROW, 20))
                    .child(SlotGroupWidget.rect(2, 2, i -> {
                        var out = i >= recipe.out.size() ? ItemStack.EMPTY : recipe.out.get(i);
                        if (out == null) out = ItemStack.EMPTY;
                        return RecipeViewerSlotWidget.create()
                                .recipeSlotRole(RecipeSlotRole.OUTPUT)
                                .value(out);
                    }));
        }
    }

    public static class EMI {

        public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ModularUI.id("machine"), EmiStack.of(TestRegistration.TEST_MACHINE_BLOCK_ITEM.get()));

        public static void register(EmiRegistry registry) {
            registry.addCategory(CATEGORY);
            Recipes.list.stream()
                    .map(r -> new RecipeDisplay(() -> Recipes.buildViewerUI(r), r))
                    .forEach(registry::addRecipe);
        }

        public static class RecipeDisplay extends ModularUIEmiRecipe {

            private final Recipe recipe;
            @Getter private final List<EmiIngredient> inputs;
            @Getter private final List<EmiStack> outputs;

            public RecipeDisplay(Supplier<IWidget> widgetSupplier, Recipe recipe) {
                super(recipe.resloc, widgetSupplier);
                this.recipe = recipe;
                this.inputs = recipe.in.stream().map(EmiStack::of).map(s -> (EmiIngredient) s).toList();
                this.outputs = recipe.out.stream().map(EmiStack::of).toList();
            }

            @Override
            public EmiRecipeCategory getCategory() {
                return CATEGORY;
            }
        }
    }
}
