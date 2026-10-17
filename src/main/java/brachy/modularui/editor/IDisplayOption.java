package brachy.modularui.editor;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IDisplayOption<V> {

    IWidget createDisplay(Supplier<V> valueGetter, Consumer<V> valueSetter);

    default <T> IWidget createDisplay(Option<T, V> option, T instance) {
        return createDisplay(() -> option.getField(instance), v -> option.setField(instance, v));
    }

    IDisplayOption<Boolean> SWITCH = (valueGetter, valueSetter) -> new ToggleButton()
            .value(new BoolValue.Dynamic(valueGetter::get, valueSetter::accept))
            .stateOverlay(GuiTextures.CHECK_BOX)
            .size(18);

    IDisplayOption<Integer> INT = (valueGetter, valueSetter) -> new TextFieldWidget()
            .value(new IntValue.Dynamic(valueGetter::get, valueSetter::accept))
            .expanded()
            .height(16)
            .setNumbers();

    IDisplayOption<String> TEXT = (valueGetter, valueSetter) -> new TextFieldWidget()
            .value(StringValue.Dynamic.nullSafe(true, valueGetter, valueSetter))
            .expanded()
            .height(16);
}
