package brachy.modularui.widget;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.PanelIdentifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record PanelModification(PanelIdentifier target, List<WidgetModification> modifications) {

    public static final Codec<PanelModification> CODEC = RecordCodecBuilder.create(i -> i.group(
            PanelIdentifier.CODEC.fieldOf("target").forGetter(PanelModification::target),
            WidgetModification.CODEC.listOf().fieldOf("modifications").forGetter(PanelModification::modifications)
    ).apply(i, PanelModification::new));

    public void apply(PanelIdentifier identifier, ModularPanel<?> panel) {
        for (WidgetModification modification : this.modifications) {
            modification.applyModification(identifier, panel);
        }
    }
}
