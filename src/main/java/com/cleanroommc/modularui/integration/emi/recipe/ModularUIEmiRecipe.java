package com.cleanroommc.modularui.integration.emi.recipe;

import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.integration.emi.EmiStackConverter;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeSlotRole;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerScreenWrapper;
import com.cleanroommc.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.cleanroommc.modularui.integration.recipeviewer.handlers.fluid.EmptyFluidTank;
import com.cleanroommc.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.WidgetUtil;
import com.cleanroommc.modularui.utils.memoization.MemoizedSupplier;
import com.cleanroommc.modularui.utils.memoization.Memoizer;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public abstract class ModularUIEmiRecipe<T extends Recipe<?>, W extends IWidget> implements EmiRecipe {

    @Getter
    protected final T recipe;
    protected final MemoizedSupplier<ModularScreen> screen;

    @Getter
    public final List<EmiIngredient> inputs;
    @Getter
    public final List<EmiStack> outputs;
    @Getter
    public final List<EmiIngredient> catalysts;

    @Getter
    private final Bounds bounds;
    @Getter
    private final int displayWidth, displayHeight;

    public boolean allowRecipeTree = true;

    public ModularUIEmiRecipe(T recipe, Supplier<W> widgetSupplier) {
        this.recipe = recipe;

        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.catalysts = new ArrayList<>();

        W recipeWidget = widgetSupplier.get();
        this.displayWidth = recipeWidget.getArea().width;
        this.displayHeight = recipeWidget.getArea().height;
        this.bounds = new Bounds(0, 0, this.displayWidth, this.displayHeight);

        this.screen = Memoizer.memoize(() -> {
            W widget = widgetSupplier.get();
            ModularPanel panel = ModularPanel.defaultPanel(recipe.getId().toString(), widget.getArea().w(), widget.getArea().h());
            panel.child(widget);
            return new ModularScreen(recipe.getId().getNamespace(), panel);
        }, Duration.ofSeconds(10));

        for (IWidget widget : WidgetUtil.getFlatWidgetCollection(recipeWidget)) {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                continue;
            }
            RecipeSlotRole role = provider.recipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) {
                continue;
            }

            EmiStackConverter.Converter<?> converter = EmiStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                continue;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            EmiIngredient ingredient = ((EmiStackConverter.Converter) converter).convertTo(provider);

            switch (role) {
                case INPUT -> inputs.add(ingredient);
                case OUTPUT -> {
                    if (ingredient.getEmiStacks().size() > 1) {
                        allowRecipeTree = false;
                    }
                    outputs.addAll(ingredient.getEmiStacks());
                }
                case CATALYST -> catalysts.add(ingredient);
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new UIWrapperWidget());

        for (IWidget widget : WidgetUtil.getFlatWidgetCollection(this.screen.get())) {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                continue;
            }
            RecipeSlotRole role = provider.recipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) {
                continue;
            }
            EmiStackConverter.Converter<?> converter = EmiStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                continue;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            EmiIngredient ingredient = ((EmiStackConverter.Converter) converter).convertTo(provider);
            Area widgetArea = widget.getArea();

            SlotWidget slotWidget = null;
            // Clear the MUI slots and add EMI slots based on them.
            if (provider instanceof ItemSlot itemSlot) {
                itemSlot.slot(RecipeScreenRenderingUtil.EMPTY_ITEM_HANDLER, 0)
                        .invisible();
            } else if (provider instanceof FluidSlot fluidSlot) {
                fluidSlot.syncHandler(EmptyFluidTank.INSTANCE)
                        .invisible();

                long capacity = Math.max(1, ingredient.getAmount());
                slotWidget = new TankWidget(ingredient, widgetArea.x, widgetArea.y, widgetArea.width, widgetArea.height,
                        capacity);
            }
            if (slotWidget == null) {
                slotWidget = new SlotWidget(ingredient, widgetArea.x, widgetArea.y);
            }

            slotWidget.customBackground(null, widgetArea.x, widgetArea.y, widgetArea.width, widgetArea.height)
                    .drawBack(false);

            if (role == RecipeSlotRole.CATALYST) {
                slotWidget.catalyst(true);
            } else if (role == RecipeSlotRole.OUTPUT) {
                slotWidget.recipeContext(this);
            }
            if (widget instanceof ITooltip<?> tooltip && tooltip.hasTooltip()) {
                if (tooltip.tooltip().getRichText() instanceof RichText richText) {
                    var textList = richText.getAsText();
                    for (FormattedText line : textList) {
                        slotWidget
                                .appendTooltip(() -> ClientTooltipComponent.create(Language.getInstance().getVisualOrder(line)));
                    }
                }
            }
            widgets.add(slotWidget);
        }
        widgets.add(new UIForegroundRenderWidget());
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.recipe.getId();
    }

    @Override
    public boolean supportsRecipeTree() {
        return this.allowRecipeTree && EmiRecipe.super.supportsRecipeTree();
    }

    public class UIWrapperWidget extends Widget {

        public UIWrapperWidget() {
            ModularScreen screen = ModularUIEmiRecipe.this.screen.get();
            screen.construct(new RecipeViewerScreenWrapper(screen));
        }

        @Override
        public Bounds getBounds() {
            return ModularUIEmiRecipe.this.bounds;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = ModularUIEmiRecipe.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            return screen.get().onMousePressed(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return screen.get().keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public class UIForegroundRenderWidget extends Widget {

        public UIForegroundRenderWidget() {}

        @Override
        public Bounds getBounds() {
            return ModularUIEmiRecipe.this.bounds;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = ModularUIEmiRecipe.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }
    }
}
