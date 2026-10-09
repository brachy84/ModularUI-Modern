package brachy.modularui.widget;

import brachy.modularui.ModularUI;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DrawableType;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.PanelIdentifier;
import brachy.modularui.utils.serialization.json.JsonHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import com.mojang.serialization.JsonOps;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WidgetSerializer {

    private static final Map<ResourceLocation, IWidget> widgetTrees = new Object2ObjectOpenHashMap<>();
    private static final Map<PanelIdentifier, List<PanelModification>> modifications = new Object2ObjectOpenHashMap<>();

    public static IWidget loadWidget(ResourceLocation rl) {
        IWidget w = widgetTrees.get(rl);
        if (w != null) {
            return w.copy();
        }
        return null;
    }

    @ApiStatus.Internal
    public static void applyModifications(PanelIdentifier id, ModularPanel<?> panel) {
        if (modifications.isEmpty()) return;
        for (PanelModification mod : modifications.getOrDefault(id, Collections.emptyList())) {
            mod.apply(id, panel);
        }
        for (PanelModification mod : modifications.getOrDefault(id.withMainPanel("*"), Collections.emptyList())) {
            mod.apply(id, panel);
        }
    }

    @ApiStatus.Internal
    public static class ReloadDataListener extends SimpleJsonResourceReloadListener {

        public static final ReloadDataListener INSTANCE = new ReloadDataListener();

        private ReloadDataListener() {
            super(JsonHelper.GSON, "mui");
            // make sure codec registries are initialized
            DrawableType.init();
            WidgetType.init();
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            for (var e : object.entrySet()) {
                var rl = e.getKey();
                if (rl.getPath().startsWith("modifications/")) {
                    var m = PanelModification.CODEC.parse(JsonOps.INSTANCE, e.getValue());
                    var res = m.result();
                    if (res.isEmpty()) {
                        ModularUI.LOGGER.error("Error reading panel modification at {}: {}", rl, m.error().orElseThrow().message());
                        continue;
                    }
                    modifications.computeIfAbsent(res.get().target(), k -> new ArrayList<>()).add(res.get());
                    continue;
                }

                var w = IWidget.CODEC.parse(JsonOps.INSTANCE, e.getValue());
                var res = w.result();
                if (res.isEmpty()) {
                    ModularUI.LOGGER.error("Error reading widget at {}: {}", rl, w.error().orElseThrow().message());
                    continue;
                }
                widgetTrees.put(e.getKey(), res.get());
            }
        }
    }
}
