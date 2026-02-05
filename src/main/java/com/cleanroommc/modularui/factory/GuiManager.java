package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.api.XeiSettings;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.ModularNetwork;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.ContainerScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainerMenu;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.ScreenWrapper;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ModularUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiManager {

    private static final Object2ObjectMap<ResourceLocation, UIFactory<?>> FACTORIES = new Object2ObjectOpenHashMap<>(
            16);

    private static final List<Player> openedContainers = new ArrayList<>(4);

    public static void registerFactory(UIFactory<?> factory) {
        Objects.requireNonNull(factory);
        ResourceLocation name = Objects.requireNonNull(factory.getFactoryName());
        if (FACTORIES.containsKey(name)) {
            throw new IllegalArgumentException("Factory with name '" + name + "' is already registered!");
        }
        FACTORIES.put(name, factory);
    }

    public static @NotNull UIFactory<?> getFactory(ResourceLocation name) {
        UIFactory<?> factory = FACTORIES.get(name);
        if (factory == null) throw new NoSuchElementException("No UI factory for name '" + name + "' found!");
        return factory;
    }

    public static boolean hasFactory(ResourceLocation name) {
        return FACTORIES.containsKey(name);
    }

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData,
                                                ServerPlayer player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create menu
        UISettings settings = new UISettings(XeiSettings.DUMMY);
        settings.defaultCanInteractWith(factory, guiData);
        ModularSyncManager msm = new ModularSyncManager(false);
        PanelSyncManager syncManager = new PanelSyncManager(msm, true);
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);

        // create the menu
        player.nextContainerCounter();
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        int windowId = player.containerCounter;
        ModularContainerMenu menu = settings.hasCustomContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        menu.construct(player, msm, settings, panel.getName(), guiData);

        // sync to client
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        int nid = ModularNetwork.SERVER.activate(msm);
        NetworkHandler.sendToPlayer(player, new OpenGuiPacket<>(windowId, nid, factory, buffer));
        // open the menu // this mimics forge behaviour
        player.initMenu(menu);
        player.containerMenu = menu;
        // init mui syncer
        msm.onOpen();
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, menu));
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(int windowId, int networkId, @NotNull UIFactory<T> factory,
                                                          @NotNull FriendlyByteBuf data, @NotNull LocalPlayer player) {
        T guiData = factory.readGuiData(player, data);
        UISettings settings = new UISettings();
        settings.defaultCanInteractWith(factory, guiData);
        ModularSyncManager msm = new ModularSyncManager(true);
        PanelSyncManager syncManager = new PanelSyncManager(msm, true);
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setSettings(settings);
        ModularContainerMenu container = settings.hasCustomContainer() ? settings.createContainer(windowId) :
                factory.createContainer(windowId);
        container.construct(player, msm, settings, panel.getName(), guiData);
        IMuiScreen wrapper = settings.hasCustomGui() ? settings.createGui(container, screen) :
                factory.createScreenWrapper(container, screen);
        if (!(wrapper.wrappedScreen() instanceof AbstractContainerScreen<?> guiContainer)) {
            throw new IllegalStateException("The wrapping screen must be a GuiContainer for synced GUIs!");
        }
        if (guiContainer.getMenu() != container)
            throw new IllegalStateException("Custom Containers are not yet allowed!");
        ModularNetwork.CLIENT.activate(networkId, msm);
        MCHelper.setScreen(wrapper.wrappedScreen());
        player.containerMenu = guiContainer.getMenu();
        msm.onOpen();
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void openFromClient(@NotNull UIFactory<T> factory, @NotNull T guiData) {
        // notify server to open the gui
        // server will send packet back to actually open the gui
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        NetworkHandler.sendToServer(new OpenGuiPacket<>(0, 0, factory, buffer));
    }

    @OnlyIn(Dist.CLIENT)
    static void openScreen(ModularScreen screen, UISettings settings) {
        if (screen.getScreenWrapper() != null &&
                MCHelper.getCurrentScreen() == screen.getScreenWrapper().wrappedScreen()) {
            // already open
            return;
        }
        screen.getContext().setSettings(settings);
        Screen guiScreen;
        if (settings.hasCustomContainer()) {
            ModularContainerMenu container = settings.createContainer(0);
            container.constructClientOnly();
            guiScreen = new ContainerScreenWrapper(container, screen);
        } else {
            guiScreen = new ScreenWrapper(screen);
        }
        MCHelper.setScreen(guiScreen);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            openedContainers.clear();
        }
    }
}
