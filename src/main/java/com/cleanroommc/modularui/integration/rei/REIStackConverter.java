package com.cleanroommc.modularui.integration.rei;

import com.cleanroommc.modularui.integration.recipeviewer.entry.EntryList;
import com.cleanroommc.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import com.cleanroommc.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import com.cleanroommc.modularui.integration.recipeviewer.entry.item.ItemStackList;
import com.cleanroommc.modularui.integration.recipeviewer.entry.item.ItemTagList;
import com.cleanroommc.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.cleanroommc.modularui.utils.MathUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * Custom EntryStack -> vanilla/neoforge/mod stack converters
 */
public class REIStackConverter {

    public static final Map<Class<?>, Converter<?>> CONVERTERS = new Reference2ReferenceOpenHashMap<>();

    public static final Converter<ItemStack> ITEM = register(ItemStack.class, new Converter<>() {

        @Override
        public @Nullable ItemStack convertFrom(EntryStack<?> stack) {
            EntryType<?> type = stack.getType();
            if (type != VanillaEntryTypes.ITEM) {
                return null;
            }
            return stack.castValue();
        }

        private static EntryIngredient toREIIngredient(Stream<ItemStack> stream) {
            return EntryIngredient.of(stream
                    .map(EntryStacks::of)
                    .toList());
        }

        @Override
        public EntryIngredient convertTo(EntryList<ItemStack> stack, float chance,
                                         UnaryOperator<ItemStack> mapper) {
            if (stack.isEmpty()) {
                return EntryIngredient.empty();
            }
            if (stack instanceof ItemStackList stackList) {
                return toREIIngredient(stackList.stream().map(mapper));
            } else if (stack instanceof ItemTagList entryList) {
                return EntryIngredient.of(entryList.getEntries().stream()
                        .map(ItemTagList.ItemTagEntry::stacks)
                        .flatMap(stream -> toREIIngredient(stream.map(mapper)).stream())
                        .collect(Collectors.toList()));
            }
            return EntryIngredient.empty();
        }
    });
    public static final Converter<FluidStack> FLUID = register(FluidStack.class, new Converter<>() {

        @Override
        public @Nullable FluidStack convertFrom(EntryStack<?> stack) {
            EntryType<?> type = stack.getType();
            if (type != VanillaEntryTypes.FLUID) {
                return null;
            }
            dev.architectury.fluid.FluidStack fluidStack = stack.castValue();
            return new FluidStack(fluidStack.getFluid(), MathUtil.saturatedCast(fluidStack.getAmount()),
                    fluidStack.getTag());
        }

        private static dev.architectury.fluid.FluidStack toREIStack(FluidStack stack) {
            return dev.architectury.fluid.FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getTag());
        }

        private static EntryIngredient toREIIngredient(Stream<FluidStack> stream) {
            return EntryIngredient.of(stream
                    .map(stack -> toREIStack(stack))
                    .map(EntryStacks::of)
                    .toList());
        }

        @Override
        public EntryIngredient convertTo(EntryList<FluidStack> stack, float chance,
                                         UnaryOperator<FluidStack> mapper) {
            if (stack.isEmpty()) {
                return EntryIngredient.empty();
            }
            if (stack instanceof FluidStackList stackList) {
                return toREIIngredient(stackList.stream().map(mapper));
            } else if (stack instanceof FluidTagList tagList) {
                return EntryIngredient.of(tagList.getEntries().stream()
                        .map(FluidTagList.FluidTagEntry::stacks)
                        .flatMap(val -> toREIIngredient(val.map(mapper)).stream())
                        .collect(Collectors.toList()));
            }
            return EntryIngredient.empty();
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

    public interface Converter<T> {

        @Nullable
        T convertFrom(EntryStack<?> stack);

        EntryIngredient convertTo(EntryList<T> stack, float chance, UnaryOperator<T> mapper);

        default EntryIngredient convertTo(IngredientProvider<T> slot) {
            return this.convertTo(slot.getIngredients(), slot.chance(), slot.renderMappingFunction());
        }
    }
}
