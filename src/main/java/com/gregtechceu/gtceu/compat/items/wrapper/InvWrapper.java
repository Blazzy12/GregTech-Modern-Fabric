package com.gregtechceu.gtceu.compat.items.wrapper;

import com.gregtechceu.gtceu.compat.items.IItemHandlerModifiable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Exposes a vanilla {@link Container} as an item handler. */
public class InvWrapper implements IItemHandlerModifiable {

    private final Container inv;

    public InvWrapper(Container inv) {
        this.inv = inv;
    }

    public Container getInv() {
        return inv;
    }

    @Override
    public int getSlots() {
        return inv.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.getItem(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack existing = inv.getItem(slot);
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) return stack;
            if (!inv.canPlaceItem(slot, stack)) return stack;
            int max = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            int space = max - existing.getCount();
            if (space <= 0) return stack;
            int moved = Math.min(space, stack.getCount());
            if (!simulate) {
                inv.setItem(slot, existing.copyWithCount(existing.getCount() + moved));
                inv.setChanged();
            }
            return moved >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
        }
        if (!inv.canPlaceItem(slot, stack)) return stack;
        int max = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
        int moved = Math.min(max, stack.getCount());
        if (!simulate) {
            inv.setItem(slot, stack.copyWithCount(moved));
            inv.setChanged();
        }
        return moved >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        ItemStack existing = inv.getItem(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;
        int taken = Math.min(amount, existing.getCount());
        if (simulate) return existing.copyWithCount(taken);
        ItemStack result = inv.removeItem(slot, taken);
        inv.setChanged();
        return result;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        inv.setItem(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inv.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inv.canPlaceItem(slot, stack);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InvWrapper other && other.inv == inv;
    }

    @Override
    public int hashCode() {
        return inv.hashCode();
    }
}
