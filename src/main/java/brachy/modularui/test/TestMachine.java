package brachy.modularui.test;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IDoubleValue;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.handlers.fluid.CombinedFluidHandlerWrapper;
import brachy.modularui.utils.handlers.fluid.IMultiTankFluidHandler;
import brachy.modularui.utils.handlers.fluid.MultiTankFluidHandler;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.SingleChildWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.DropdownWidget;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public class TestMachine {

    private static final int FLUID_SLOT_CAPACITY = 10_000;

    public static class BE extends AbstractBlockEntity implements IUIHolder<PosGuiData> {

        private final ItemStackHandler inputItems = new ItemStackHandler(4);
        private final ItemStackHandler outputItems = new ItemStackHandler(4);
        private final MultiTankFluidHandler inputFluids = new MultiTankFluidHandler(2, FLUID_SLOT_CAPACITY);
        private final MultiTankFluidHandler outputFluids = new MultiTankFluidHandler(2, FLUID_SLOT_CAPACITY);
        private final LazyOptional<IItemHandlerModifiable> items = LazyOptional.of(() -> new CombinedInvWrapper(inputItems, outputItems));
        private final LazyOptional<IFluidHandler> fluids = LazyOptional.of(() -> new CombinedFluidHandlerWrapper(inputFluids, outputFluids));

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
                return this.items.cast();
            } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
                return this.fluids.cast();
            }
            return super.getCapability(cap, side);
        }

        @Override
        public ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
            return new ModularScreen(ModularUI.MOD_ID, mainPanel);
        }

        @Override
        public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
            var panel = new ModularPanel<>("machine");
            return panel
                    .coverChildren()
                    .invisible()
                    .child(Flow.col()
                            .coverChildren()
                            .childPadding(2)
                            .child(new ParentWidget<>()
                                    .coverChildren(176, 30)
                                    .padding(7)
                                    .widgetTheme(IThemeApi.PANEL)
                                    .child(Recipes.buildMachineUI(panel, this.inputItems, this.outputItems, this.inputFluids, this.outputFluids, new DoubleSyncValue(this::getProgress), false))
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
                                                    .value(new BooleanSyncValue(() -> this.paused, v -> this.paused = v).allowC2S())
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
                    if (++this.recipeProgress >= this.lastRecipe.ticks) {
                        this.lastRecipe.finishRecipe(this.outputItems, this.outputFluids);
                        this.recipeProgress = 0;
                        if (this.lastRecipe.startRecipe(this.inputItems, this.inputFluids, true)) {
                            this.lastRecipe.startRecipe(this.inputItems, this.inputFluids, false);
                        } else {
                            this.running = false;
                        }
                    }
                } else if (this.ticks % 20 == 0) {
                    Recipe recipe = Recipes.findRecipe(this.inputItems, this.inputFluids, this.lastRecipe);
                    if (recipe != null) {
                        this.lastRecipe = recipe;
                        this.recipeProgress = 0;
                        this.lastRecipe.startRecipe(this.inputItems, this.inputFluids, false);
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

        protected final ResourceLocation id;
        protected final List<ItemStack> inItems = new ArrayList<>(), outItems = new ArrayList<>();
        protected final List<FluidStack> inFluids = new ArrayList<>(), outFluids = new ArrayList<>();
        private int ticks = 80;

        public Recipe(String name) {
            this.id = ModularUI.id(name);
        }

        public Recipe itemIn(ItemStack stack) {
            if (!stack.isEmpty()) this.inItems.add(stack);
            return this;
        }

        public Recipe itemIn(Item item, int count) {
            return itemIn(new ItemStack(item, count));
        }

        public Recipe itemIn(Item item) {
            return itemIn(item, 1);
        }

        public Recipe itemOut(ItemStack stack) {
            if (!stack.isEmpty()) this.outItems.add(stack);
            return this;
        }

        public Recipe itemOut(Item item, int count) {
            return itemOut(new ItemStack(item, count));
        }

        public Recipe itemOut(Item item) {
            return itemOut(item, 1);
        }

        public Recipe fluidIn(FluidStack stack) {
            if (!stack.isEmpty()) this.inFluids.add(stack);
            return this;
        }

        public Recipe fluidIn(Fluid fluid, int count) {
            return fluidIn(new FluidStack(fluid, count));
        }

        public Recipe fluidIn(Fluid fluid) {
            return fluidIn(fluid, FluidType.BUCKET_VOLUME);
        }

        public Recipe fluidOut(FluidStack stack) {
            if (!stack.isEmpty()) this.outFluids.add(stack);
            return this;
        }

        public Recipe fluidOut(Fluid fluid, int count) {
            return fluidOut(new FluidStack(fluid, count));
        }

        public Recipe fluidOut(Fluid fluid) {
            return fluidOut(fluid, FluidType.BUCKET_VOLUME);
        }

        public Recipe ticks(int ticks) {
            this.ticks = ticks;
            return this;
        }

        public boolean startRecipe(IItemHandler itemHandler, IFluidHandler fluidHandler, boolean simulate) {
            for (ItemStack in : this.inItems) {
                if (!Recipes.extract(in, itemHandler, simulate)) {
                    return false;
                }
            }
            for (FluidStack in : this.inFluids) {
                if (!Recipes.extract(in, fluidHandler, simulate)) {
                    return false;
                }
            }
            return true;
        }

        public void finishRecipe(IItemHandler itemHandler, IFluidHandler fluidHandler) {
            for (ItemStack out : this.outItems) {
                ItemHandlerHelper.insertItemStacked(itemHandler, out, false);
            }
            for (FluidStack out : this.outFluids) {
                fluidHandler.fill(out.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public static class Recipes {

        public static final List<Recipe> list = Util.make(new ArrayList<>(), list -> {
            list.add(new Recipe("/stuff_to_nether_star")
                    .itemIn(Items.DIAMOND)
                    .itemIn(Items.EMERALD)
                    .itemIn(Items.GOLD_INGOT, 4)
                    .itemOut(Items.NETHER_STAR));

            // add (many) more recipes with (consistent) random inputs and outputs to try to overflow the cache for testing
            final int VERY_MANY_RECIPES = 256;
            RandomSource rng = RandomSource.create("""
                      // chosen by fair dice roll.
                      // guaranteed to be random.
                    """.hashCode());

            // start counting from 1 so we get exactly 256 recipes
            for (int i = 1; i < VERY_MANY_RECIPES; i++) {
                int inItems = rng.nextIntBetweenInclusive(1, 4), inFluids = rng.nextIntBetweenInclusive(1, 4);
                int outItems = rng.nextIntBetweenInclusive(1, 4), outFluids = rng.nextIntBetweenInclusive(1, 4);
                Recipe recipe = new Recipe("/random_" + i)
                        .ticks(rng.nextIntBetweenInclusive(1, 512));

                while (recipe.inItems.size() < inItems) {
                    BuiltInRegistries.ITEM.getRandom(rng).ifPresent(item -> {
                        ItemStack stack = item.value().getDefaultInstance();
                        stack.setCount(Mth.clamp((int) (rng.nextGaussian() * stack.getMaxStackSize()), 0, stack.getMaxStackSize()));
                        recipe.itemIn(stack);
                    });
                }
                while (recipe.outItems.size() < outItems) {
                    BuiltInRegistries.ITEM.getRandom(rng).ifPresent(item -> {
                        ItemStack stack = item.value().getDefaultInstance();
                        stack.setCount(Mth.clamp((int) (rng.nextGaussian() * stack.getMaxStackSize()), 0, stack.getMaxStackSize()));
                        recipe.itemOut(stack);
                    });
                }
                while (recipe.inFluids.size() < inFluids) {
                    BuiltInRegistries.FLUID.getRandom(rng).ifPresent(fluid -> {
                        int amount = Mth.clamp((int) (rng.nextGaussian() * FLUID_SLOT_CAPACITY), 0, FLUID_SLOT_CAPACITY);
                        if (amount == 0) return;

                        recipe.fluidIn(fluid.value(), amount);
                    });
                }
                while (recipe.outFluids.size() < outFluids) {
                    BuiltInRegistries.FLUID.getRandom(rng).ifPresent(fluid -> {
                        int amount = Mth.clamp((int) (rng.nextGaussian() * FLUID_SLOT_CAPACITY), 0, FLUID_SLOT_CAPACITY);
                        if (amount == 0) return;

                        recipe.fluidOut(fluid.value(), amount);
                    });
                }

                list.add(recipe);
            }
        });

        public static Recipe findRecipe(IItemHandler inputItems, IFluidHandler inputFluids, @Nullable Recipe lastRecipe) {
            if (lastRecipe != null && lastRecipe.startRecipe(inputItems, inputFluids, true)) return lastRecipe;
            for (Recipe recipe : list) {
                if (recipe.startRecipe(inputItems, inputFluids, true)) {
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

        private static boolean extract(FluidStack stack, IFluidHandler handler, boolean simulate) {
            if (stack.isEmpty()) return true;
            FluidStack c = handler.drain(stack.getAmount(), IFluidHandler.FluidAction.SIMULATE);
            if (stack.isFluidEqual(c)) {
                if (!simulate) {
                    c = handler.drain(stack.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                }
                return c.getAmount() >= stack.getAmount();
            }

            return false;
        }

        public static IWidget buildMachineUI(ModularPanel<?> panel, IItemHandler inItems, IItemHandler outItems,
                                             IMultiTankFluidHandler inFluids, IMultiTankFluidHandler outFluids, IDoubleValue<?> progress,
                                             final boolean isRecipeViewerUI) {
            var val = new StringValue("Option 1");
            IPanelHandler panelHandler = IPanelHandler.simple(panel, (parent, player) -> {
                return new ModularPanel<>("test_sub_panel").size(50).overlay(Text.str("Test"));
            }, true);
            return Flow.col()
                    .coverChildren()
                    .childPadding(2)
                    .child(new DropdownWidget<>("test_drop_down", String.class)
                            .width(70)
                            .height(14)
                            .value(val)
                            .option("Option 1")
                            .option("Option 2")
                            .option("Option 3")
                            .optionToWidget((s, b) -> Text.str(s).asWidget().center().color(Color.WHITE.main).shadow(true).padding(2)))
                    .child(new ButtonWidget<>()
                            .size(70, 14)
                            .overlay(Text.str("Sub panel"))
                            .onMousePressed((ctx, button) -> {
                                panelHandler.openPanel();
                                return true;
                            }))
                    .child(Flow.row().name("slots")
                            .coverChildren()
                            .childPadding(8)
                            .child(Flow.col().name("input")
                                    .coverChildren()
                                    .child(SlotGroupWidget.rect(2, 2, i -> new ItemSlot()
                                            .slot(new ModularSlot(inItems, i))
                                            .recipeRole(RecipeSlotRole.INPUT)))
                                    .child(SlotGroupWidget.rect(2, 1, i -> new FluidSlot()
                                            .syncHandler(new FluidSlotSyncHandler(inFluids, i))
                                            .recipeRole(RecipeSlotRole.INPUT))))
                            .child(new ProgressWidget()
                                    .value(progress)
                                    .size(20)
                                    .texture(GuiTextures.PROGRESS_ARROW, ProgressDrawable.Direction.RIGHT)
                                    .configure(w -> {
                                        if (!isRecipeViewerUI) {
                                            w.listenGuiAction((IGuiAction.MouseReleased) (ctx, button) -> {
                                                if (!ctx.isMouseAbove(w)) return false;
                                                TestRecipeViewerGuis.openTestRecipeViewerCategory();
                                                return true;
                                            });
                                        }
                                    }))
                            .child(Flow.col().name("output")
                                    .coverChildren()
                                    .child(SlotGroupWidget.rect(2, 2, i -> new ItemSlot()
                                            .slot(new ModularSlot(outItems, i).canPut(false).canDragInto(false))
                                            .recipeRole(RecipeSlotRole.OUTPUT)))
                                    .child(SlotGroupWidget.rect(2, 1, i -> new FluidSlot()
                                            .syncHandler(new FluidSlotSyncHandler(outFluids, i).canFillSlot(false))
                                            .recipeRole(RecipeSlotRole.OUTPUT)))));
        }
    }
}
