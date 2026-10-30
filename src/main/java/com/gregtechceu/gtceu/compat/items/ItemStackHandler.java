package com.gregtechceu.gtceu.compat.items;

import com.gregtechceu.gtceu.compat.common.util.INBTSerializable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ItemStackHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void setSize(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;
        validateSlotIndex(slot);

        ItemStack existing = stacks.get(slot);
        int limit = getStackLimit(slot, stack);
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0) return stack;

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);

        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            return existing.copy();
        }
        if (!simulate) {
            stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 99;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag items = new ListTag();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;
            int slot = i;
            ItemStack.CODEC.encodeStart(ops, stack).ifSuccess(tag -> {
                CompoundTag entry = (CompoundTag) tag;
                entry.putInt("Slot", slot);
                items.add(entry);
            });
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", items);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setSize(nbt.getIntOr("Size", stacks.size()));
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        ListTag items = nbt.getListOrEmpty("Items");
        for (Tag tag : items) {
            if (!(tag instanceof CompoundTag entry)) continue;
            int slot = entry.getIntOr("Slot", -1);
            if (slot < 0 || slot >= stacks.size()) continue;
            ItemStack.CODEC.parse(ops, entry).ifSuccess(stack -> stacks.set(slot, stack));
        }
        onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}
}
