package brachy.modularui.widget;

import brachy.modularui.ModularUI;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.PanelIdentifier;

import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

public record WidgetModification(String target, Operation operation, int index, IWidget widget) {

    public static final Codec<WidgetModification> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("target").forGetter(WidgetModification::target),
            Operation.CODEC.fieldOf("operation").forGetter(WidgetModification::operation),
            Codec.INT.optionalFieldOf("index", -1).forGetter(WidgetModification::index),
            IWidget.CODEC.optionalFieldOf("widget", null).forGetter(WidgetModification::widget)
    ).apply(i, WidgetModification::new));

    public void applyModification(PanelIdentifier panelIdentifier, ModularPanel<?> panel) {
        var d = WidgetTree.findChildAndParentAt(panel, target());
        var res = d.result();
        if (res.isEmpty()) {
            ModularUI.LOGGER.error("Failed to apply widget modification to widget {} of panel {}, because widget was not found.",
                    this.target, panelIdentifier);
            return;
        }
        var p = res.get();
        IWidget target = p.getFirst();
        IWidget handler = p.getSecond();
        if (isAdd()) handler = target;
        if (!handler.applyModification(this, target)) {
            ModularUI.LOGGER.error("Failed to apply widget modification to widget {} of panel {}, because widget {} " +
                    "can't handle the modification.", this.target, panelIdentifier, handler);
        }
    }

    public boolean isAdd() {
        return this.operation == Operation.ADD;
    }

    public boolean isRemove() {
        return this.operation == Operation.REMOVE;
    }

    public boolean isReplace() {
        return this.operation == Operation.REPLACE;
    }

    public enum Operation implements StringRepresentable {
        REMOVE,
        ADD,
        REPLACE;

        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        public final String name;

        Operation() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
