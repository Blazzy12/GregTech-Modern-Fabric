package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.items.IItemHandler;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** A GT item handler seen as a Fabric item storage. */
public final class GTItemStorage implements Storage<ItemVariant> {

    private final IItemHandler handler;

    GTItemStorage(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        long inserted = 0;
        for (int slot = 0; slot < handler.getSlots() && inserted < maxAmount; slot++) {
            int count = (int) Math.min(maxAmount - inserted, resource.getItem().getDefaultMaxStackSize());
            ItemStack remainder = handler.insertItem(slot, resource.toStack(count), false);
            int done = count - remainder.getCount();
            if (done > 0) {
                int finalSlot = slot;
                TransferBridges.undoOnAbort(transaction, () -> handler.extractItem(finalSlot, done, false));
                inserted += done;
            }
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        long extracted = 0;
        for (int slot = 0; slot < handler.getSlots() && extracted < maxAmount; slot++) {
            if (!resource.matches(handler.getStackInSlot(slot))) continue;
            ItemStack taken = handler.extractItem(slot, (int) Math.min(maxAmount - extracted, Integer.MAX_VALUE),
                    false);
            if (!taken.isEmpty()) {
                int finalSlot = slot;
                TransferBridges.undoOnAbort(transaction, () -> handler.insertItem(finalSlot, taken, false));
                extracted += taken.getCount();
            }
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            views.add(new SlotView(slot));
        }
        return views.iterator();
    }

    private final class SlotView implements StorageView<ItemVariant> {

        private final int slot;

        SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!resource.matches(handler.getStackInSlot(slot))) return 0;
            ItemStack taken = handler.extractItem(slot, (int) Math.min(maxAmount, Integer.MAX_VALUE), false);
            if (taken.isEmpty()) return 0;
            TransferBridges.undoOnAbort(transaction, () -> handler.insertItem(slot, taken, false));
            return taken.getCount();
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getStackInSlot(slot).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(handler.getStackInSlot(slot));
        }

        @Override
        public long getAmount() {
            return handler.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return handler.getSlotLimit(slot);
        }
    }
}
