package brachy.modularui.core.mixins.rei;

import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;
import brachy.modularui.integration.rei.recipe.ModularUIReiCategory;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.gui.screen.AbstractDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.display.DisplaySpec;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = DefaultDisplayViewingScreen.class, remap = false)
public abstract class DefaultDisplayViewingScreenMixin extends AbstractDisplayViewingScreen {

    protected DefaultDisplayViewingScreenMixin(Map<DisplayCategory<?>, List<DisplaySpec>> categoryMap,
                                               @Nullable CategoryIdentifier<?> category) {
        super(categoryMap, category);
    }

    @WrapOperation(method = "lambda$checkExportDisplays$18", at = @At(value = "INVOKE", target = "Lme/shedaniel/rei/api/client/registry/display/DisplayCategory;getDisplayHeight()I"))
    private int modularui$forcePerDisplayHeightInExport(DisplayCategory<?> category,
                                                        Operation<Integer> original,
                                                        @Local(name = "display") Display display) {
        return modularui$calculateCustomDisplayHeight(getCurrentCategory(), display, original::call);
    }

    @Inject(method = "initDisplays",
            at = @At(value = "INVOKE_ASSIGN", target = "Lme/shedaniel/rei/api/client/registry/display/DisplayCategory;getDisplayWidth(Lme/shedaniel/rei/api/common/display/Display;)I"))
    private void modularui$perDisplayHeight(CallbackInfo ci,
                                            @Local(name = "displayHeight") LocalIntRef displayHeight,
                                            @Local(name = "displaySupplier") Supplier<Display> displaySupplier) {
        displayHeight.set(modularui$calculateCustomDisplayHeight(getCurrentCategory(), displaySupplier.get(), null));
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static int modularui$calculateCustomDisplayHeight(DisplayCategory<?> category, Display display,
                                                              @Nullable ToIntFunction<DisplayCategory<?>> originalValue) {
        if (category instanceof ModularUIReiCategory<?> muiCategory && display instanceof ModularUIReiDisplay muiDisplay) {
            return ((ModularUIReiCategory<ModularUIReiDisplay>) muiCategory).getDisplayHeight(muiDisplay);
        } else {
            // if this isn't a MUI display, set it back to the original value
            // (I think it's possible that an MUI category has a non-MUI display in it? not sure tho.)
            if (originalValue == null) {
                // originalValue == null means we can't trust it and should get the actual original value
                return category.getDisplayHeight();
            } else {
                return originalValue.applyAsInt(category);
            }
        }
    }
}
