package com.gregtechceu.gtceu.compat.items;

import net.minecraft.world.item.ItemStack;

public interface IItemHandler {

    int getSlots();

    ItemStack getStackInSlot(int slot);

    /** @return the remainder that could not be inserted */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
