package brachy.modularui.core;

import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.FMLLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModularUIMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE = "brachy.modularui.core.mixins.";
    private static final Map<String, String> MOD_COMPAT_MIXINS = new HashMap<>();

    private static final String DEV_PACKAGE = "dev.";

    static {
        MOD_COMPAT_MIXINS.put("roughlyenoughitems", "rei.");
        MOD_COMPAT_MIXINS.put("emi", "emi.");
        MOD_COMPAT_MIXINS.put("jei", "jei.");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE)) {
            // skip checking mixins that aren't in our package
            // this should never happen, but better safe than sorry
            return true;
        }
        if (!FMLLoader.getLoadingModList().getErrors().isEmpty()) {
            // stop processing mixins if we have load errors to avoid getting bad crash reports in our issues
            return false;
        }
        mixinClassName = mixinClassName.substring(MIXIN_PACKAGE.length());

        if (mixinClassName.startsWith(DEV_PACKAGE)) {
            // don't load dev-only mixins in prod
            return !FMLLoader.isProduction();
        }
        for (var compatMod : MOD_COMPAT_MIXINS.entrySet()) {
            if (mixinClassName.startsWith(compatMod.getValue())) {
                return isModLoaded(compatMod.getKey());
            }
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }
}
