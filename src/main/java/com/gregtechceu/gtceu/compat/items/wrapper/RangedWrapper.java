package com.gregtechceu.gtceu.compat.items.wrapper;

import com.gregtechceu.gtceu.compat.items.IItemHandlerModifiable;

import net.minecraft.world.item.ItemStack;

/** A contiguous range of slots of another handler. */
public class RangedWrapper implements IItemHandlerModifiable {

    private final IItemHandlerModifiable compose;
    private final int minSlot, maxSlot;

    public RangedWrapper(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
        this.compose = compose;
        this.minSlot = minSlot;
        this.maxSlot = maxSlotExclusive;
    }

    @Override
    public int getSlots() {
        return maxSlot - minSlot;
    }

    private boolean inRange(int slot) {
        return slot >= 0 && slot < getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inRange(slot) ? compose.getStackInSlot(slot + minSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return inRange(slot) ? compose.insertItem(slot + minSlot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inRange(slot) ? compose.extractItem(slot + minSlot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (inRange(slot)) compose.setStackInSlot(slot + minSlot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inRange(slot) ? compose.getSlotLimit(slot + minSlot) : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inRange(slot) && compose.isItemValid(slot + minSlot, stack);
    }
}
