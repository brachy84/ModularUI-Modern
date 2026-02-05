package com.cleanroommc.modularui;

import com.cleanroommc.modularui.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Mod(ModularUI.MOD_ID)
public class ModularUI {

    public static final String MOD_ID = "modularui";
    public static final String NAME = "ModularUI";
    private static final ResourceLocation TEMPLATE_LOCATION = new ResourceLocation(MOD_ID, "");

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public ModularUI() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    public static ResourceLocation id(String path) {
        if (path.isBlank()) {
            return TEMPLATE_LOCATION;
        }

        int i = path.indexOf(':');
        if (i > 0) {
            return new ResourceLocation(path);
        } else if (i == 0) {
            path = path.substring(i + 1);
        }
        // only convert it to camel_case if it has any uppercase to begin with
        if (FormattingUtil.hasUpperCase(path)) {
            path = FormattingUtil.toLowerCaseUnderscore(path);
        }
        return TEMPLATE_LOCATION.withPath(path);
    }

    /**
     * @return if we're running in a production environment
     */
    public static boolean isProd() {
        return FMLLoader.isProduction();
    }

    /**
     * @return if we're not running in a production environment
     */
    public static boolean isDev() {
        return !isProd();
    }

    /**
     * @return if we're running data generation
     */
    public static boolean isDataGen() {
        return FMLLoader.getLaunchHandler().isData();
    }

    /**
     * A friendly reminder that the server instance is populated on the server side only, so null/side check it!
     *
     * @return the current minecraft server instance
     */
    public static MinecraftServer getMinecraftServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    /**
     * @param modId the mod id to check for
     * @return if the mod whose id is {@code modId} is loaded or not
     */
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * For async stuff use this, otherwise use {@link ModularUI#isClientSide}
     *
     * @return if the current thread is the client thread
     */
    public static boolean isClientThread() {
        return isClientSide() && Minecraft.getInstance().isSameThread();
    }

    /**
     * @return if the game is the <strong>PHYSICAL</strong> client, e.g. not a dedicated server.
     * @apiNote Do not use this to check if you're currently on the server thread for side-specific actions!
     * It does <strong>NOT</strong> work for that. Use {@link #isClientThread()} instead.
     * @see #isClientThread()
     */
    public static boolean isClientSide() {
        return FMLEnvironment.dist.isClient();
    }

    /**
     * This check isn't the same for client and server!
     *
     * @return if it's safe to access the current instance {@link net.minecraft.world.level.Level Level} on client or if
     * it's safe to access any level on server.
     */
    public static boolean canGetServerLevel() {
        if (isClientSide()) {
            return Minecraft.getInstance().level != null;
        }
        var server = getMinecraftServer();
        return server != null &&
                !(server.isStopped() || server.isShutdown() || !server.isRunning() || server.isCurrentlySaving());
    }

    /**
     * @return the path to the minecraft instance directory
     */
    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess FROZEN = BLANK;

    /**
     * You shouldn't call it, you should probably not even look at it just to be extra safe
     *
     * @param registryAccess the new value to set to the frozen registry access
     */
    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        FROZEN = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (isClientThread()) {
            return ClientHelpers.getClientRegistries();
        }
        return FROZEN;
    }

    private static class ClientHelpers {

        private static RegistryAccess getClientRegistries() {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            } else {
                return FROZEN;
            }
        }
    }

    public enum Mods {

        //BLUR(ModIds.BLUR),
        //BOGOSORTER(ModIds.BOGOSORTER),
        CURIOS(ModIds.CURIOS),
        EMI(ModIds.EMI),
        JEI(ModIds.JEI),
        REI(ModIds.REI),
        KUBEJS(ModIds.KUBEJS),
        //MODNAMETOOLTIP(ModIds.MODNAMETOOLTIP)
        //NEA(ModIds.NEA),
        EMBEDDIUM(ModIds.EMBEDDIUM),
        SODIUM(ModIds.SODIUM),
        IRIS(ModIds.IRIS),
        OCULUS(ModIds.OCULUS);

        public static boolean isSodiumLikeLoaded() {
            return SODIUM.isLoaded() || EMBEDDIUM.isLoaded();
        }

        public static boolean isIrisLikeLoaded() {
            return IRIS.isLoaded() || OCULUS.isLoaded();
        }

        public static boolean isRecipeViewerLoaded() {
            return JEI.isLoaded() || EMI.isLoaded() || REI.isLoaded();
        }

        public final String id;
        private boolean loaded = false;
        private boolean initialized = false;
        private final Predicate<ModContainer> extraLoadedCheck;

        Mods(String id) {
            this(id, null);
        }

        Mods(String id, @Nullable Predicate<ModContainer> extraLoadedCheck) {
            this.id = id;
            this.extraLoadedCheck = extraLoadedCheck;
        }

        public boolean isLoaded() {
            if (!this.initialized) {
                this.loaded = ModList.get().isLoaded(this.id);
                if (this.loaded && this.extraLoadedCheck != null) {
                    this.loaded = this.extraLoadedCheck.test(ModList.get().getModContainerById(this.id).orElseThrow());
                }
                this.initialized = true;
            }
            return this.loaded;
        }
    }

    public static class ModIds {

        public static final String BLUR = "blur";
        public static final String BOGOSORTER = "bogosorter";
        public static final String CURIOS = "curios";
        public static final String EMI = "emi";
        public static final String JEI = "jei";
        public static final String REI = "roughlyenoughitems";
        public static final String KUBEJS = "kubejs";
        public static final String MODNAMETOOLTIP = "modnametooltip";
        public static final String NEA = "neverenoughanimations";
        public static final String IRIS = "iris";
        public static final String OCULUS = "oculus";
        public static final String SODIUM = "sodium";
        public static final String EMBEDDIUM = "embeddium";
    }
}
