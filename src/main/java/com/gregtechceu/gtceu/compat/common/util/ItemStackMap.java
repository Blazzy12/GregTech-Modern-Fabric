package com.gregtechceu.gtceu.compat.common.util;

import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.Map;

public final class ItemStackMap {

    private static final Hash.Strategy<ItemStack> TYPE_AND_TAG = new Hash.Strategy<>() {

        @Override
        public int hashCode(ItemStack stack) {
            return ItemStack.hashItemAndComponents(stack);
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            return a == b || a != null && b != null && ItemStack.isSameItemSameComponents(a, b);
        }
    };

    private ItemStackMap() {}

    /** A map keyed by item and components, ignoring count. */
    public static <V> Map<ItemStack, V> createTypeAndTagMap() {
        return new Object2ObjectOpenCustomHashMap<>(TYPE_AND_TAG);
    }
}
