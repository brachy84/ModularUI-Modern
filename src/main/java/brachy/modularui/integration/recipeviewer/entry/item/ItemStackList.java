package brachy.modularui.integration.recipeviewer.entry.item;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public record ItemStackList(@Getter List<ItemStack> getStacks) implements ItemEntryList {

    public ItemStackList() {
        this(new ArrayList<>());
    }

    public ItemStackList(List<ItemStack> getStacks) {
        this.getStacks = new ArrayList<>(getStacks);
    }

    public static ItemStackList of(ItemStack stack) {
        var list = new ItemStackList();
        list.add(stack);
        return list;
    }

    public static ItemStackList of(Collection<ItemStack> coll) {
        var list = new ItemStackList();
        list.addAll(coll);
        return list;
    }

    public void add(ItemStack stack) {
        getStacks.add(stack);
    }

    public void addAll(Collection<ItemStack> list) {
        getStacks.addAll(list);
    }

    @Override
    public boolean isEmpty() {
        return getStacks.isEmpty();
    }

    public Stream<ItemStack> stream() {
        return getStacks.stream();
    }
}
