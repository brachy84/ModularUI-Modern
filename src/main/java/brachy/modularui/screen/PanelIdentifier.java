package brachy.modularui.screen;

import brachy.modularui.utils.serialization.codec.CodecUtil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import com.mojang.serialization.Lifecycle;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.NotNull;

public record PanelIdentifier(String screen, String mainPanel, String targetPanel) {

    public static final Codec<PanelIdentifier> RECORD_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("screen").forGetter(PanelIdentifier::screen),
            Codec.STRING.fieldOf("mainPanel").forGetter(PanelIdentifier::mainPanel),
            Codec.STRING.fieldOf("targetPanel").forGetter(PanelIdentifier::targetPanel)
    ).apply(i, PanelIdentifier::new));

    public static final Codec<PanelIdentifier> STRING_CODEC = Codec.STRING.comapFlatMap(PanelIdentifier::read, PanelIdentifier::toString);
    public static final Codec<PanelIdentifier> CODEC = CodecUtil.chainedCodec(STRING_CODEC, RECORD_CODEC);

    public static PanelIdentifier mainPanel(String screen, String panel) {
        return new PanelIdentifier(screen, panel, panel);
    }

    public static PanelIdentifier of(ModularScreen screen, ModularPanel<?> targetPanel) {
        return new PanelIdentifier(screen.getOwner(), screen.getName(), targetPanel.getName());
    }

    public static DataResult<PanelIdentifier> read(String s) {
        int i = s.indexOf(':');
        if (i < 0) {
            return DataResult.error(() -> "Panel identified must contain at least one 1 ':'");
        }
        String screen = s.substring(0, i);
        int j = s.indexOf(':', i + 1);
        String mainPanel, targetPanel;
        if (j < 0) {
            mainPanel = s.substring(i + 1);
            targetPanel = mainPanel;
        } else {
            mainPanel = s.substring(i + 1, j);
            if (s.indexOf(':', j + 1) > 0) {
                return DataResult.error(() -> "Panel identified must not contain more than 2 ':'");
            }
            targetPanel = s.substring(j + 1);
        }
        if (screen.isEmpty()) return DataResult.error(() -> "Screen name is empty");
        if (mainPanel.isEmpty()) return DataResult.error(() -> "Main panel name is empty");
        if (targetPanel.isEmpty()) return DataResult.error(() -> "Target panel name is empty");
        return DataResult.success(new PanelIdentifier(screen, mainPanel, targetPanel), Lifecycle.stable());
    }

    public boolean isMainPanel() {
        return mainPanel.equals(targetPanel);
    }

    public PanelIdentifier withScreen(String screen) {
        return new PanelIdentifier(screen, this.mainPanel, this.targetPanel);
    }

    public PanelIdentifier withMainPanel(String mainPanel) {
        return new PanelIdentifier(this.screen, mainPanel, this.targetPanel);
    }

    public PanelIdentifier withTargetPanel(String targetPanel) {
        return new PanelIdentifier(this.screen, this.mainPanel, targetPanel);
    }

    @Override
    public @NotNull String toString() {
        return this.screen + ":" + this.mainPanel + ":" + this.targetPanel;
    }
}
