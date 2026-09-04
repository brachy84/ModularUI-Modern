package brachy.modularui.integration.emi;

import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.*;
import brachy.modularui.integration.recipeviewer.entry.item.*;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.utils.math.MathUtils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom {@link EmiStack} <-> vanilla/neoforge/mod stack converters
 */
public class EmiStackConverter {

    public static final Map<Class<?>, Converter<?>> CONVERTERS = new Reference2ReferenceOpenHashMap<>();

    public static final Converter<ItemStack> ITEM = register(ItemStack.class, new Converter<>() {

        @Override
        public @Nullable ItemStack convertFrom(EmiStack stack) {
            Item key = stack.getKeyOfType(Item.class);
            if (key == null || key == Items.AIR) {
                return null;
            }
            ItemStack itemStack = new ItemStack(key, MathUtils.saturatedCast(stack.getAmount()));
            itemStack.setTag(stack.getNbt());
            return itemStack;
        }

        private static EmiIngredient toEMIIngredient(Stream<ItemStack> stream) {
            return EmiIngredient.of(stream.map(EmiStack::of).toList());
        }

        @Override
        public EmiIngredient convertTo(EntryList<ItemStack> stack, float chance, UnaryOperator<ItemStack> mapper) {
            if (stack.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (stack instanceof ItemStackList stackList) {
                return toEMIIngredient(stackList.stream().map(mapper)).setChance(chance);
            } else if (stack instanceof ItemTagList tagList) {
                return EmiIngredient.of(tagList.getEntries().stream()
                                        .map(ItemTagList.ItemTagEntry::stacks)
                                        .map(stream -> toEMIIngredient(stream.map(mapper)))
                                        .collect(Collectors.toList()),
                                tagList.getEntries().get(0).amount())
                        .setChance(chance);
            } else if (stack instanceof ItemHolderSetList holderSetList) {
                return EmiIngredient.of(holderSetList.getEntries().stream()
                                        .map(ItemHolderSetList.ItemHolderSetEntry::stacks)
                                        .map(stream -> toEMIIngredient(stream.map(mapper)))
                                        .collect(Collectors.toList()),
                                holderSetList.getEntries().get(0).amount())
                        .setChance(chance);
            }
            return EmiStack.EMPTY;
        }
    });
    public static final Converter<FluidStack> FLUID = register(FluidStack.class, new Converter<>() {

        @Override
        public @Nullable FluidStack convertFrom(EmiStack stack) {
            Fluid key = stack.getKeyOfType(Fluid.class);
            if (key != null && key != Fluids.EMPTY) {
                return new FluidStack(key, MathUtils.saturatedCast(stack.getAmount()), stack.getNbt());
            }
            var item = ITEM.convertFrom(stack);
            if (item != null) {
                return item.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                        .filter(f -> f.getTanks() == 1)
                        .map(f -> f.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE)).orElse(null);
            }
            return null;
        }

        private static EmiIngredient toEMIIngredient(Stream<FluidStack> stream) {
            return EmiIngredient.of(stream.map(ForgeEmiStack::of).toList());
        }

        @Override
        public EmiIngredient convertTo(EntryList<FluidStack> stack, float chance, UnaryOperator<FluidStack> mapper) {
            if (stack.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (stack instanceof FluidStackList stackList) {
                return toEMIIngredient(stackList.stream().map(mapper)).setChance(chance);
            } else if (stack instanceof FluidTagList tagList) {
                return EmiIngredient.of(tagList.getEntries().stream()
                                        .map(FluidTagList.FluidTagEntry::stacks)
                                        .map(stream -> toEMIIngredient(stream.map(mapper)))
                                        .collect(Collectors.toList()),
                                tagList.getEntries().get(0).amount())
                        .setChance(chance);
            } else if (stack instanceof FluidHolderSetList holderSetList) {
                return EmiIngredient.of(holderSetList.getEntries().stream()
                                        .map(FluidHolderSetList.FluidHolderSetEntry::stacks)
                                        .map(stream -> toEMIIngredient(stream.map(mapper)))
                                        .collect(Collectors.toList()),
                                holderSetList.getEntries().get(0).amount())
                        .setChance(chance);
            }
            return EmiStack.EMPTY;
        }
    });

    public static <T> Converter<T> register(Class<T> clazz, Converter<T> converter) {
        CONVERTERS.put(clazz, converter);
        return converter;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> Converter<T> getForNullable(Class<T> clazz) {
        return (Converter<T>) CONVERTERS.get(clazz);
    }

    public static <T> Optional<Converter<T>> getFor(Class<T> clazz) {
        return Optional.ofNullable(getForNullable(clazz));
    }

    public static <T> EmiIngredient convertToEmiEntry(EntryList<T> entries, float chance, UnaryOperator<T> renderMappingFunction) {
        Converter<T> converter = getForNullable(entries.getType());
        if (converter != null) {
            return converter.convertTo(entries, chance, renderMappingFunction);
        }
        return EmiStack.EMPTY;
    }

    public interface Converter<T> {

        @Nullable
        T convertFrom(EmiStack stack);

        EmiIngredient convertTo(EntryList<T> stack, float chance, UnaryOperator<T> mapper);

        default EmiIngredient convertTo(IngredientProvider<T> slot) {
            return this.convertTo(slot.getIngredients(), slot.chance(), slot.renderMappingFunction());
        }
    }
}
