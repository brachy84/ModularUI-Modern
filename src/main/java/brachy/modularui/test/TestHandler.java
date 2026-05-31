package brachy.modularui.test;

import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIConfig;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.ClientGUI;
import brachy.modularui.screen.CustomModularScreen;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.OpenScreenEvent;
import brachy.modularui.screen.event.RichTooltipEvent;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.ReloadThemeEvent;
import brachy.modularui.theme.SelectableTheme;
import brachy.modularui.theme.ThemeBuilder;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.datafixers.util.Either;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ModularUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestHandler {

    public static boolean enabledRichTooltipEventTest = false;
    public static final String TEST_THEME = "mui:test_theme";
    private static final ThemeBuilder<?> testTheme = new ThemeBuilder<>(TEST_THEME)
            .defaultColor(Color.BLUE_ACCENT.brighter(0))
            .widgetTheme(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>()
                    .color(Color.BLUE_ACCENT.brighter(0))
                    .selectedColor(Color.WHITE.main)
                    .selectedIconColor(Color.RED.brighter(0)))
            .widgetThemeHover(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>()
                    .selectedIconColor(Color.DEEP_PURPLE.brighter(0)))
            .textColor(IThemeApi.TEXT_FIELD, Color.DEEP_PURPLE.main);

    private static final IIcon tooltipLine = new IDrawable() {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            int high = Color.PURPLE.main;
            int low = Color.withAlpha(high, 0.05f);
            GuiDraw.drawHorizontalGradientRect(context.getGraphics(), x, y + 1, width / 2f, 1, low, high);
            GuiDraw.drawHorizontalGradientRect(context.getGraphics(), x + width / 2f, y + 1, width / 2f, 1, high, low);
        }
    }.asIcon().height(3);

    private static List<ItemStack> allItems = null;

    public static ItemStack getRandomItem() {
        if (allItems == null) {
            allItems = new ArrayList<>();
            for (Item item : ForgeRegistries.ITEMS) {
                allItems.add(new ItemStack(item));
            }
        }
        return allItems.get(new Random().nextInt(allItems.size())).copy();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level().isClientSide && ModularUI.isDev()) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.getItem() == Items.DIAMOND) {
                ClientGUI.open(new TestGuis());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRichTooltip(RichTooltipEvent.Gather.Post event) {
        if (enabledRichTooltipEventTest && ModularUI.isDev()) {
            // adds decoration to every mui tooltip
            event.getTooltip()
                    .moveCursorToEnd()
                    .newLine()
                    .add(Text.str("Powered By: ").style(Text.GOLD, Text.ITALIC))
                    .addDrawable(GuiTextures.MUI_LOGO.asIcon().size(18)).newLine()
                    .moveCursorToStart()
                    .moveCursorToNextLine()
                    .addDrawableLine(tooltipLine)
                    // replaces the Minecraft mod name in JEI item tooltips
                    .replace("Minecraft", key -> Text.str("Chicken Jockey").style(Text.BLUE, Text.ITALIC))
                    .moveCursorToEnd();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onVanillaTooltip(RenderTooltipEvent.GatherComponents event) {
        if (enabledRichTooltipEventTest && event.getItemStack().getItem() == Items.DIAMOND) {
            // adds a tooltip line to diamond even inside mui uis
            event.getTooltipElements().add(Either.left(Text.str("Hello from ModularUI").style(Text.GOLD)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onThemeReload(ReloadThemeEvent.Pre event) {
        if (ModularUI.isDev()) {
            IThemeApi.get().registerTheme(testTheme);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onOpenScreen(OpenScreenEvent event) {
        if (ModularUIConfig.enableTestOverlays()) {
            /*if (event.getScreen() instanceof  gui) {
                event.addOverlay(getMainMenuOverlayTest(gui));
            } else */
            if (event.getScreen() instanceof AbstractContainerScreen<?> gui) {
                event.addOverlay(getContainerOverlayTest(gui));
            }
        }
    }

    /*private ModularScreen getMainMenuOverlayTest(GuiMainMenu gui) {
        TextWidget<?> title = new TextWidget<>(IKey.str("ModularUI"));
        int[] colors = {Color.WHITE.main, Color.AMBER.main, Color.BLUE.main, Color.GREEN.main, Color.DEEP_PURPLE.main, Color.RED.main};
        AtomicInteger k = new AtomicInteger();
        return new ModularScreen(ModularUI.ID,
                ModularPanel.defaultPanel("overlay")
                        .fullScreenInvisible()
                        .child(title.scale(5f)
                                .shadow(true)
                                .color(colors[k.get()])
                                .leftRel(0.5f).topRel(0.07f))
                        .child(new ButtonWidget<>() // test button overlapping
                                .topRel(0.25f, 59, 0f)
                                .leftRelOffset(0.5f, 91)
                                .size(44)
                                .overlay(IKey.str("Fun Button"))
                                .onMousePressed(mouseButton -> {
                                    k.set((k.get() + 1) % colors.length);
                                    title.color(colors[k.get()]);
                                    return true;
                                })));
    }*/

    @OnlyIn(Dist.CLIENT)
    private static ModularScreen getContainerOverlayTest(AbstractContainerScreen<?> gui) {
        return new CustomModularScreen(ModularUI.MOD_ID) {

            @Override
            public @NotNull ModularPanel<?> buildUI(ModularGuiContext context) {
                return ModularPanel.defaultPanel("watermark_overlay", gui.getXSize(), gui.getYSize())
                        .pos(gui.getGuiLeft(), gui.getGuiTop())
                        .invisible()
                        .child(GuiTextures.MUI_LOGO.asIcon().asWidget()
                                .top(5).right(5)
                                .size(18));
            }

            @Override
            public void onResize(int width, int height) {
                getMainPanel().pos(gui.getGuiLeft(), gui.getGuiTop())
                        .size(gui.getXSize(), gui.getYSize());
                super.onResize(width, height);
            }
        };
    }
}
