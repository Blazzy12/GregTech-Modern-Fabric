package com.gregtechceu.gtceu.core.fabric;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** {@code ItemStack.save/parse}, which 26.x removed in favour of codecs. */
public final class ItemStackNbt {

    private ItemStackNbt() {}

    /** Saves a non-empty stack; an empty stack becomes an empty compound. */
    public static Tag save(ItemStack stack, HolderLookup.Provider registries) {
        if (stack.isEmpty()) return new CompoundTag();
        return ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .getOrThrow();
    }

    public static Tag saveOptional(ItemStack stack, HolderLookup.Provider registries) {
        return save(stack, registries);
    }

    public static Optional<ItemStack> parse(HolderLookup.Provider registries, Tag tag) {
        return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result();
    }

    public static ItemStack parseOptional(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag.isEmpty()) return ItemStack.EMPTY;
        return parse(registries, tag).orElse(ItemStack.EMPTY);
    }
}
