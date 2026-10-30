package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.items.IItemHandler;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/** A Fabric item storage seen as a GT item handler. Unslotted storages get an extra slot that inserts anywhere. */
public final class FabricItemHandler implements IItemHandler {

    private final Storage<ItemVariant> storage;

    FabricItemHandler(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    private List<StorageView<ItemVariant>> views() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        storage.forEach(views::add);
        return views;
    }

    private Storage<ItemVariant> slotStorage(int slot) {
        if (storage instanceof SlottedStorage<ItemVariant> slotted) {
            return slot >= 0 && slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
        }
        return storage;
    }

    private StorageView<ItemVariant> view(int slot) {
        if (storage instanceof SlottedStorage<ItemVariant> slotted) {
            return slot >= 0 && slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
        }
        List<StorageView<ItemVariant>> views = views();
        return slot >= 0 && slot < views.size() ? views.get(slot) : null;
    }

    @Override
    public int getSlots() {
        if (storage instanceof SlottedStorage<ItemVariant> slotted) return slotted.getSlotCount();
        return views().size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        StorageView<ItemVariant> view = view(slot);
        if (view == null || view.isResourceBlank()) return ItemStack.EMPTY;
        return view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !storage.supportsInsertion()) return stack;
        Storage<ItemVariant> target = slotStorage(slot);
        if (target == null) return stack;
        try (Transaction transaction = TransferBridges.open()) {
            long inserted = target.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (!simulate) transaction.commit();
            return inserted == 0 ? stack : stack.copyWithCount(stack.getCount() - (int) inserted);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        StorageView<ItemVariant> view = view(slot);
        if (amount <= 0 || view == null || view.isResourceBlank()) return ItemStack.EMPTY;
        ItemVariant resource = view.getResource();
        try (Transaction transaction = TransferBridges.open()) {
            long extracted = view.extract(resource, amount, transaction);
            if (!simulate) transaction.commit();
            return extracted == 0 ? ItemStack.EMPTY : resource.toStack((int) extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        StorageView<ItemVariant> view = view(slot);
        return view == null ? 64 : (int) Math.min(Integer.MAX_VALUE, view.getCapacity());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
