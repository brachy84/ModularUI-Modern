package com.cleanroommc.modularui;

import net.minecraft.ChatFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

import com.cleanroommc.modularui.screen.RichTooltip;

import java.util.Objects;

public class ModularUIConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG;

    static {
        BUILDER.push("ui");
    }
    public static final IntValue DEFAULT_SCROLL_SPEED = BUILDER
            .comment("Amount of pixels scrolled")
            .translation("config.modularui.defaultScrollSpeed")
            .defineInRange("defaultScrollSpeed", 30, 1, 100);
    public static final BooleanValue SMOOTH_PROGRESS_BARS = BUILDER
            .comment("If progress bars should step in texture pixels or screen pixels. (Screen pixels are way smaller and therefore smoother)")
            .translation("config.modularui.smoothProgressBars")
            .define("smoothProgressBars", false);
    public static final IntValue ANIMATION_TIME = BUILDER
            .comment("Duration of UI animations in ms.", "Default: 100")
            .translation("config.modularui.animationTime")
            .defineInRange("animationTime", 100, 0, 500);
    // Default direction
    public static final EnumValue<RichTooltip.Pos> TOOLTIP_POS = BUILDER
            .comment("Default tooltip position around the widget or its panel.")
            .translation("config.modularui.tooltipPos")
            .defineEnum("tooltipPos", RichTooltip.Pos.NEXT_TO_MOUSE);
    public static final BooleanValue ESC_RESTORES_LAST_TEXT = BUILDER
            .comment("If true, pressing ESC key in the text field will restore the last text instead of confirming current one.")
            .translation("config.modularui.escRestoresLastText")
            .define("escRestoresLastText", false);
    public static final BooleanValue USE_DARK_THEME_BY_DEFAULT = BUILDER
            .comment("If true and not specified otherwise, screens will try to use the 'vanilla_dark' theme.")
            .translation("config.modularui.useDarkThemeByDefault")
            .define("useDarkThemeByDefault", false);
    public static final BooleanValue ENABLE_TEST_GUIS = BUILDER
            .comment("Enables a test block, test item with a test gui and opening a gui by right clicking a diamond.")
            .translation("config.modularui.enableTestGuis")
            .worldRestart() //.gameRestart()
            .define("enableTestGuis", ModularUI.isDev());
    public static final BooleanValue ENABLE_TEST_OVERLAYS = BUILDER
            .comment("Enables a test overlay shown on title screen and watermark shown on every GuiContainer.")
            .translation("config.modularui.enableTestOverlays")
            .worldRestart() //.gameRestart()
            .define("enableTestOverlays", false);
    public static final BooleanValue REPLACE_VANILLA_TOOLTIPS = BUILDER
            .comment("If true, vanilla tooltip will be replaced with MUI's RichTooltip")
            .translation("config.modularui.replaceVanillaTooltips")
            .define("replaceVanillaTooltips", false);
    public static final ConfigValue<String> MOD_NAME_FORMAT = BUILDER
            .comment("The format prefix of the mod name tooltip line.", "Default: 'blue italic' (converted to §9§o)")
            .translation("config.modularui.modNameFormat")
            .define("modNameFormat", ChatFormatting.BLUE.getName() + " " + ChatFormatting.ITALIC.getName());
    static {
        BUILDER.pop().push("dev");
    }

    public static final BooleanValue DEBUG_UI = BUILDER
            .comment("Debug UI? (Will draw widget outlines and widget information)", "Default: false")
            .translation("config.modularui.dev.debugUI")
            .define("debugUI", ModularUI.isDev());
    public static final ConfigValue<String> TEXT_COLOR = BUILDER
            .comment("Color for debug text, in #AARRGGBB")
            .translation("config.modularui.dev.textColor")
            .define("textColor", "#DCB42873");
    public static final ConfigValue<String> OUTLINE_COLOR = BUILDER
            .comment("Color for outlining widgets in debug mode, in #AARRGGBB")
            .translation("config.modularui.dev.outlineColor")
            .define("outlineColor", "#DCB42873");
    public static final ConfigValue<String> CURSOR_COLOR = BUILDER
            .comment("Color for cursor in debug mode, in #AARRGGBB")
            .translation("config.modularui.dev.cursorColor")
            .define("cursorColor", "#FF4CAF50");
    public static final DoubleValue SCALE = BUILDER
            .comment("Scale of debug text", "Default: 0.8f")
            .translation("config.modularui.dev.debugTextScale")
            .defineInRange("scale", 0.8, 0.1, 10.0);

    public static final BooleanValue SHOW_HOVERED = BUILDER
            .translation("config.modularui.dev.showHovered")
            .define("showHovered", true);
    public static final BooleanValue SHOW_POS = BUILDER
            .translation("config.modularui.dev.showPos")
            .define("showPos", true);
    public static final BooleanValue SHOW_SIZE = BUILDER
            .translation("config.modularui.dev.showSize")
            .define("showSize", true);
    public static final BooleanValue SHOW_WIDGET_THEME = BUILDER
            .translation("config.modularui.dev.showWidgetTheme")
            .define("showWidgetTheme", true);
    public static final BooleanValue SHOW_EXTRA = BUILDER
            .translation("config.modularui.dev.showExtra")
            .define("showExtra", true);
    public static final BooleanValue SHOW_OUTLINE = BUILDER
            .translation("config.modularui.dev.showOutline")
            .define("showOutline", true);

    public static final BooleanValue SHOW_PARENT = BUILDER
            .translation("config.modularui.dev.showParent")
            .define("showParent", true);
    public static final BooleanValue SHOW_PARENT_POS = BUILDER
            .translation("config.modularui.dev.showParentPos")
            .define("showParentPos", true);
    public static final BooleanValue SHOW_PARENT_SIZE = BUILDER
            .translation("config.modularui.dev.showParentSize")
            .define("showParentSize", true);
    public static final BooleanValue SHOW_PARENT_WIDGET_THEME = BUILDER
            .translation("config.modularui.dev.showParentWidgetTheme")
            .define("showParentWidgetTheme", true);
    public static final BooleanValue SHOW_PARENT_OUTLINE = BUILDER
            .translation("config.modularui.dev.showParentOutline")
            .define("showParentOutline", true);

    static {
        BUILDER.pop();
        CONFIG = BUILDER.build();
    }

    public static int defaultScrollSpeed() {
        return DEFAULT_SCROLL_SPEED.get();
    }

    public static boolean smoothProgressBars() {
        return SMOOTH_PROGRESS_BARS.get();
    }

    public static int animationTime() {
        return ANIMATION_TIME.get();
    }

    public static RichTooltip.Pos tooltipPos() {
        return TOOLTIP_POS.get();
    }

    public static boolean escRestoresLastText() {
        return ESC_RESTORES_LAST_TEXT.get();
    }

    public static boolean useDarkThemeByDefault() {
        return USE_DARK_THEME_BY_DEFAULT.get();
    }

    public static boolean enableTestGuis() {
        return ENABLE_TEST_GUIS.get();
    }

    public static boolean enableTestOverlays() {
        return ENABLE_TEST_OVERLAYS.get();
    }

    public static boolean replaceVanillaTooltips() {
        return REPLACE_VANILLA_TOOLTIPS.get();
    }

    private static String lastValue = null;
    private static ChatFormatting[] lastParsed = null;

    public static ChatFormatting[] getModNameFormat() {
        String unparsed = MOD_NAME_FORMAT.get();
        if (!Objects.equals(unparsed, lastValue)) {
            lastValue = unparsed;
            String[] split = lastValue.split("\\s");
            lastParsed = new ChatFormatting[split.length];
            for (int i = 0; i < split.length; i++) {
                String name = split[i];
                lastParsed[i] = ChatFormatting.getByName(name);
            }
        }
        return lastParsed;
    }

    public static final class Dev {

        private Dev() {}

        public static boolean debugUI() {
            return DEBUG_UI.get();
        }

        public static int textColor() {
            return Long.decode(TEXT_COLOR.get()).intValue();
        }

        public static int outlineColor() {
            return Long.decode(OUTLINE_COLOR.get()).intValue();
        }

        public static int cursorColor() {
            return Long.decode(CURSOR_COLOR.get()).intValue();
        }

        public static float scale() {
            return SCALE.get().floatValue();
        }

        public static boolean showHovered() {
            return SHOW_HOVERED.get();
        }

        public static boolean showPos() {
            return SHOW_POS.get();
        }

        public static boolean showSize() {
            return SHOW_SIZE.get();
        }

        public static boolean showWidgetTheme() {
            return SHOW_WIDGET_THEME.get();
        }

        public static boolean showExtra() {
            return SHOW_EXTRA.get();
        }

        public static boolean showOutline() {
            return SHOW_OUTLINE.get();
        }

        public static boolean showParent() {
            return SHOW_PARENT.get();
        }

        public static boolean showParentPos() {
            return SHOW_PARENT_POS.get();
        }

        public static boolean showParentSize() {
            return SHOW_PARENT_SIZE.get();
        }

        public static boolean showParentWidgetTheme() {
            return SHOW_PARENT_WIDGET_THEME.get();
        }

        public static boolean showParentOutline() {
            return SHOW_PARENT_OUTLINE.get();
        }
    }
}
