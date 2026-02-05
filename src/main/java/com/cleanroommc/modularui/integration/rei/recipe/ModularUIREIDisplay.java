package com.cleanroommc.modularui.integration.rei.recipe;

import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.client.component.FormattedTextContents;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeSlotRole;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerScreenWrapper;
import com.cleanroommc.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.cleanroommc.modularui.integration.recipeviewer.handlers.fluid.EmptyFluidTank;
import com.cleanroommc.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import com.cleanroommc.modularui.integration.rei.REIStackConverter;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.WidgetUtil;
import com.cleanroommc.modularui.utils.memoization.MemoizedSupplier;
import com.cleanroommc.modularui.utils.memoization.Memoizer;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import lombok.Getter;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class ModularUIREIDisplay<T extends Recipe<?>, W extends IWidget> implements Display {

    @Getter
    protected final T recipe;
    protected final MemoizedSupplier<ModularScreen> screen;

    @Getter
    protected final List<EntryIngredient> inputEntries;
    @Getter
    protected final List<EntryIngredient> outputEntries;
    protected final List<EntryIngredient> catalysts;
    @Getter
    protected final CategoryIdentifier<?> categoryIdentifier;

    public ModularUIREIDisplay(T recipe, Supplier<W> widgetSupplier, CategoryIdentifier<?> category) {
        this.recipe = recipe;

        this.inputEntries = new ArrayList<>();
        this.outputEntries = new ArrayList<>();
        this.catalysts = new ArrayList<>();
        this.categoryIdentifier = category;

        this.screen = Memoizer.memoize(() -> {
            W widget = widgetSupplier.get();
            ModularPanel panel = ModularPanel.defaultPanel(recipe.getId().toString(), widget.getArea().w(), widget.getArea().h());
            panel.child(widget);
            return new ModularScreen(recipe.getId().getNamespace(), panel);
        }, Duration.ofSeconds(10));

        for (IWidget widget : WidgetUtil.getFlatWidgetCollection(widgetSupplier.get())) {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                continue;
            }
            RecipeSlotRole role = provider.recipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) {
                continue;
            }

            REIStackConverter.Converter<?> converter = REIStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                continue;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            EntryIngredient ingredient = ((REIStackConverter.Converter) converter).convertTo(provider);

            switch (role) {
                case INPUT -> inputEntries.add(ingredient);
                case OUTPUT -> outputEntries.add(ingredient);
                case CATALYST -> catalysts.add(ingredient);
            }
        }
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(this.recipe.getId());
    }

    public List<Widget> createWidgets(Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new UIWrapperWidget());

        for (IWidget widget : WidgetUtil.getFlatWidgetCollection(this.screen.get())) {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                continue;
            }
            RecipeSlotRole role = provider.recipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) {
                continue;
            }
            REIStackConverter.Converter<?> converter = REIStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                continue;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            EntryIngredient ingredient = ((REIStackConverter.Converter) converter).convertTo(provider);
            Area area = widget.getArea();

            EntryWidget entryWidget = new EntryWidget(new Rectangle(area.x(), area.y(), area.w(), area.h()));
            // Clear the MUI slots and add EMI slots based on them.
            if (provider instanceof ItemSlot itemSlot) {
                itemSlot.slot(RecipeScreenRenderingUtil.EMPTY_ITEM_HANDLER, 0).invisible();
            } else if (provider instanceof FluidSlot fluidSlot) {
                fluidSlot.syncHandler(EmptyFluidTank.INSTANCE).invisible();
            }

            entryWidget.background(false)
                    .entries(ingredient.castAsList());
            if (role == RecipeSlotRole.INPUT) {
                entryWidget.markIsInput();
            } else if (role == RecipeSlotRole.OUTPUT) {
                entryWidget.markIsOutput();
            } else {
                entryWidget.unmarkInputOrOutput();
            }
            if (widget instanceof ITooltip<?> tooltip && tooltip.hasTooltip()) {
                if (tooltip.tooltip().getRichText() instanceof RichText richText) {
                    var textList = richText.getAsText();
                    entryWidget.tooltipProcessor(text -> {
                        for (FormattedText line : textList) {
                            text = text.add(MutableComponent.create(new FormattedTextContents(line)));
                        }
                        return text;
                    });
                }
            }
            widgets.add(entryWidget);
        }
        widgets.add(new UIForegroundRenderWidget());

        return widgets;
    }

    @Override
    public List<EntryIngredient> getRequiredEntries() {
        var required = new ArrayList<>(catalysts);
        required.addAll(inputEntries);
        return required;
    }

    public class UIWrapperWidget extends Widget {

        public UIWrapperWidget() {
            ModularScreen screen = ModularUIREIDisplay.this.screen.get();
            screen.construct(new RecipeViewerScreenWrapper(screen));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = ModularUIREIDisplay.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }

        @Override
        public @Nullable Tooltip getTooltip(TooltipContext context) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            screen.get().mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return screen.get().mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return screen.get().mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return screen.get().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
            return screen.get().mouseScrolled(mouseX, mouseY, scrollDelta);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return screen.get().keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return screen.get().keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return screen.get().charTyped(codePoint, modifiers);
        }
    }

    public class UIForegroundRenderWidget extends Widget {

        public UIForegroundRenderWidget() {}

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = ModularUIREIDisplay.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }
}
