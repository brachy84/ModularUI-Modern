package brachy.modularui.widgets.slot;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.SlotTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.IMultiFluidTankHandler;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.AbstractFluidDisplayWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.ModList;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FluidSlot extends AbstractFluidDisplayWidget<FluidSlot>
        implements Interactable, GhostIngredientSlot<FluidStack>, IngredientProvider<FluidStack> {

    public static final int DEFAULT_SIZE = 18;
    public static final String UNIT_BUCKET = "B";
    public static final String UNIT_LITER = "L";
    private static final DecimalFormat TOOLTIP_FORMAT = new DecimalFormat("#.##");
    private static final IFluidTank EMPTY = new FluidTank(0);

    static {
        TOOLTIP_FORMAT.setGroupingUsed(true);
        TOOLTIP_FORMAT.setGroupingSize(3);
    }

    private FluidSlotSyncHandler syncHandler;

    @Getter private boolean alwaysShowFull = true;

    public FluidSlot() {
        tooltip().autoUpdate(true);
        tooltipBuilder(this::addTooltip);
    }

    protected void addTooltip(RichTooltip tooltip) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack fluid = this.syncHandler.getValue();
        if (fluid != null && !fluid.isEmpty()) {
            tooltip.addLine(fluid.getDisplayName()).spaceLine(2);
        }
        if (this.syncHandler.phantom()) {
            if (fluid != null) {
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(Text.lang("modularui.fluid.phantom.amount",
                            formatFluidTooltipAmount(fluid.getAmount()), getUnit()));
                }
            } else {
                tooltip.addLine(Text.lang("modularui.fluid.empty"));
                tooltip.addLine(
                        Text.lang("modularui.fluid.capacity", formatFluidTooltipAmount(fluidTank.getCapacity()),
                                getUnit()));
            }
            if (this.syncHandler.controlsAmount()) {
                tooltip.addLine(Text.lang("modularui.fluid.phantom.control"));
            }
        } else {
            if (fluid != null) {
                tooltip.addLine(Text.lang("modularui.fluid.amount", formatFluidTooltipAmount(fluid.getAmount()),
                        formatFluidTooltipAmount(fluidTank.getCapacity()), getUnit()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(Text.lang("modularui.fluid.empty"));
            }
            if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                tooltip.addLine(Text.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(Text.lang("modularui.fluid.click_combined"));
                    } else if (this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(Text.lang("modularui.fluid.click_to_fill"));
                    } else if (this.syncHandler.canFillSlot()) {
                        tooltip.addLine(Text.lang("modularui.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.addLine(Text.lang("modularui.tooltip.shift"));
                }
            }
        }
        if (fluid != null && !fluid.isEmpty()) {
            tooltip.add(getFluidModName(fluid));
        }
    }

    private Component getFluidModName(FluidStack fluidStack) {
        String modID = getFluidModID(fluidStack.getFluid());
        var container = ModList.get().getModContainerById(modID);
        if (container.isPresent()) {
            return Component.literal(container.get().getModInfo().getDisplayName()).withStyle(ChatFormatting.BLUE,
                    ChatFormatting.ITALIC);
        }
        return Component.literal(modID).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }

    public static String getFluidModID(Fluid fluid) {
        ResourceLocation modName = BuiltInRegistries.FLUID.getKey(fluid);
        return modName.getNamespace();
    }

    public void addAdditionalFluidInfo(RichTooltip tooltip, FluidStack fluidStack) {}

    public String formatFluidTooltipAmount(double amount) {
        // the tooltip show the full number
        return TOOLTIP_FORMAT.format(amount);
    }

    @Override
    public void onInit() {
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(FluidSlotSyncHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castNullable(FluidSlotSyncHandler.class);
    }

    @Override
    public @NotNull FluidSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return syncHandler;
    }

    @Override
    protected boolean displayAmountText() {
        return this.syncHandler == null || this.syncHandler.controlsAmount();
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (isHovering()) {
            RenderSystem.colorMask(true, true, true, false);
            GuiDraw.drawRect(context.getGraphics(), 1, 1, getArea().w() - 2, getArea().h() - 2, getSlotHoverColor());
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetThemeEntry<SlotTheme> theme = getWidgetTheme(getPanel().getTheme(), SlotTheme.class);
        return theme.theme().getSlotHoverColor();
    }

    @NotNull
    @Override
    public Result onMousePressed(int button) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.ACCEPT;
        }
        ItemStack cursorStack = Minecraft.getInstance().player.containerMenu.getCarried();
        if (this.syncHandler.phantom() ||
                (!cursorStack.isEmpty() &&
                        cursorStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent())) {
            MouseData mouseData = MouseData.create(button);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScrolled(double delta) {
        if (this.syncHandler.phantom()) {
            if ((delta > 0 && !this.syncHandler.canFillSlot()) || (delta < 0 && !this.syncHandler.canDrainSlot())) {
                return false;
            }
            MouseData mouseData = MouseData.create(delta > 0 ? 1 : -1);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_SCROLL, mouseData::writeToPacket);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LSHIFT || keyCode == InputConstants.KEY_RSHIFT) {
            markTooltipDirty();
        }
        return Interactable.super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LSHIFT || keyCode == InputConstants.KEY_RSHIFT) {
            markTooltipDirty();
        }
        return Interactable.super.onKeyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public int getCapacity() {
        return this.alwaysShowFull ? 0 : getFluidTank().getCapacity();
    }

    @Nullable
    public FluidStack getFluidStack() {
        return this.syncHandler == null ? null : this.syncHandler.getValue();
    }

    public IFluidTank getFluidTank() {
        return this.syncHandler == null ? EMPTY : this.syncHandler.fluidTank();
    }

    /**
     * @param alwaysShowFull if the fluid should be rendered as full or as the partial amount.
     */
    public FluidSlot alwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public FluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public FluidSlot syncHandler(IMultiFluidTankHandler fluidTank, int index) {
        return syncHandler(fluidTank.getFluidTank(index));
    }

    public FluidSlot syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    public FluidSlot tank(IFluidTank fluidTank) {
        return syncHandler(fluidTank);
    }

    public FluidSlot tank(IMultiFluidTankHandler fluidTank, int index) {
        return syncHandler(fluidTank, index);
    }

    /* === Jei ghost slot === */

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        if (this.syncHandler.phantom()) {
            this.syncHandler.setValue(ingredient);
        }
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return areAncestorsEnabled() && this.syncHandler.phantom() && ingredient instanceof FluidStack fluidStack ?
                fluidStack : null;
    }
}
