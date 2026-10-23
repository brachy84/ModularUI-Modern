package brachy.modularui.theme;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.IThemeApi;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTheme implements ITheme {

    @Getter
    private final String id;
    @Getter
    private final @Nullable ITheme parentTheme;

    private @Nullable WidgetThemeEntry<WidgetTheme> fallback;
    private @Nullable WidgetThemeEntry<WidgetTheme> panel;
    private @Nullable WidgetThemeEntry<WidgetTheme> button;
    private @Nullable WidgetThemeEntry<WidgetTheme> scrollbar;
    private @Nullable WidgetThemeEntry<SlotTheme> itemSlot;
    private @Nullable WidgetThemeEntry<SlotTheme> fluidSlot;
    private @Nullable WidgetThemeEntry<TextFieldTheme> textField;
    private @Nullable WidgetThemeEntry<SelectableTheme> toggleButtonTheme;

    protected AbstractTheme(String id, @Nullable ITheme parentTheme) {
        this.id = id;
        this.parentTheme = parentTheme;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getFallback() {
        if (fallback == null) {
            fallback = getWidgetTheme(IThemeApi.FALLBACK);
        }
        return fallback;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getPanelTheme() {
        if (panel == null) {
            panel = getWidgetTheme(IThemeApi.PANEL);
        }
        return panel;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getButtonTheme() {
        if (button == null) {
            button = getWidgetTheme(IThemeApi.BUTTON);
        }
        return button;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getScrollbarTheme() {
        if (scrollbar == null) {
            scrollbar = getWidgetTheme(IThemeApi.SCROLLBAR);
        }
        return scrollbar;
    }

    @Override
    public WidgetThemeEntry<SlotTheme> getItemSlotTheme() {
        if (itemSlot == null) {
            itemSlot = getWidgetTheme(IThemeApi.ITEM_SLOT);
        }
        return itemSlot;
    }

    @Override
    public WidgetThemeEntry<SlotTheme> getFluidSlotTheme() {
        if (fluidSlot == null) {
            fluidSlot = getWidgetTheme(IThemeApi.FLUID_SLOT);
        }
        return fluidSlot;
    }

    @Override
    public WidgetThemeEntry<TextFieldTheme> getTextFieldTheme() {
        if (textField == null) {
            textField = getWidgetTheme(IThemeApi.TEXT_FIELD);
        }
        return textField;
    }

    @Override
    public WidgetThemeEntry<SelectableTheme> getToggleButtonTheme() {
        if (toggleButtonTheme == null) {
            toggleButtonTheme = getWidgetTheme(IThemeApi.TOGGLE_BUTTON);
        }
        return toggleButtonTheme;
    }
}
