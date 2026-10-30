package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.capabilities.Capabilities;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;
import com.gregtechceu.gtceu.compat.items.IItemHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Connects GregTech's item and fluid handlers with Fabric's Transfer API, both ways: GT machines and pipes see
 * vanilla and other mods' storages as handlers, and hoppers and other mods see GT's handlers as storages.
 * <p>
 * Each direction only asks the other when the block has nothing of its own; a thread-local flag stops the two
 * fallbacks from asking each other in a loop.
 */
public final class TransferBridges {

    private static final ThreadLocal<Boolean> BRIDGING = ThreadLocal.withInitial(() -> false);

    private TransferBridges() {}

    public static void init() {
        Capabilities.ItemHandler.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, side) -> bridged(
                () -> {
                    Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, side);
                    return storage == null || storage instanceof GTItemStorage ? null :
                            new FabricItemHandler(storage);
                }));
        ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) -> bridged(() -> {
            IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(level, pos, state, blockEntity, side);
            return handler == null || handler instanceof FabricItemHandler ? null : new GTItemStorage(handler);
        }));
        Capabilities.FluidHandler.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, side) -> bridged(
                () -> {
                    Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, blockEntity, side);
                    return storage == null || storage instanceof GTFluidStorage ? null :
                            new FabricFluidHandler(storage);
                }));
        FluidStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) -> bridged(() -> {
            IFluidHandler handler = Capabilities.FluidHandler.BLOCK.getCapability(level, pos, state, blockEntity,
                    side);
            return handler == null || handler instanceof FabricFluidHandler ? null : new GTFluidStorage(handler);
        }));
    }

    private static <T> T bridged(java.util.function.Supplier<T> lookup) {
        if (BRIDGING.get()) return null;
        BRIDGING.set(true);
        try {
            return lookup.get();
        } finally {
            BRIDGING.set(false);
        }
    }

    /** Opens a transaction for a GT-side operation, nested in the caller's one if there is any. */
    static Transaction open() {
        if (Transaction.isOpen()) {
            TransactionContext current = Transaction.getCurrentUnsafe();
            if (current != null) return current.openNested();
        }
        return Transaction.openOuter();
    }

    /**
     * Runs {@code undo} if {@code transaction}, or one it is nested in, is aborted. GT handlers change right away,
     * so an aborted transaction is rolled back by doing the opposite.
     */
    static void undoOnAbort(TransactionContext transaction, Runnable undo) {
        transaction.addCloseCallback((closed, result) -> {
            if (result.wasAborted()) {
                undo.run();
            } else if (closed.nestingDepth() > 0) {
                undoOnAbort(closed.getOpenTransaction(closed.nestingDepth() - 1), undo);
            }
        });
    }
}
