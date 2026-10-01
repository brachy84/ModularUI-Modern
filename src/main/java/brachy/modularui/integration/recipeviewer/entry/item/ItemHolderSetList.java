package brachy.modularui.integration.recipeviewer.entry.item;

import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ItemHolderSetList implements ItemEntryList {

    @Getter
    private final List<ItemHolderSetEntry> entries = new ArrayList<>();

    public static ItemHolderSetList of(@NonNull HolderSet<Item> set, int amount, @Nullable CompoundTag nbt) {
        var list = new ItemHolderSetList();
        list.add(set, amount, nbt);
        return list;
    }

    public void add(ItemHolderSetEntry entry) {
        entries.add(entry);
    }

    public void add(@NonNull HolderSet<Item> set, int amount, @Nullable CompoundTag nbt) {
        add(new ItemHolderSetEntry(set, amount, nbt));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<ItemStack> getStacks() {
        return entries.stream()
                .flatMap(ItemHolderSetEntry::stacks)
                .toList();
    }

    public record ItemHolderSetEntry(@NonNull HolderSet<Item> set, int amount, @Nullable CompoundTag nbt) {

        public Stream<ItemStack> stacks() {
            return set.stream().map(holder -> ItemTagList.stackWithTag(holder, amount, nbt));
        }
    }
}
