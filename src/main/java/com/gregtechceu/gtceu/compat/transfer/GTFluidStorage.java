package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.gregtechceu.gtceu.compat.transfer.FabricFluidHandler.DROPLETS_PER_MB;

/** A GT fluid handler seen as a Fabric fluid storage, in whole millibuckets. */
public final class GTFluidStorage implements Storage<FluidVariant> {

    private final IFluidHandler handler;

    GTFluidStorage(IFluidHandler handler) {
        this.handler = handler;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        long millibuckets = maxAmount / DROPLETS_PER_MB;
        if (resource.isBlank() || millibuckets <= 0) return 0;
        FluidStack stack = FabricFluidHandler.stack(resource, millibuckets);
        int filled = handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) return 0;
        TransferBridges.undoOnAbort(transaction,
                () -> handler.drain(stack.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE));
        return filled * DROPLETS_PER_MB;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        long millibuckets = maxAmount / DROPLETS_PER_MB;
        if (resource.isBlank() || millibuckets <= 0) return 0;
        FluidStack drained = handler.drain(FabricFluidHandler.stack(resource, millibuckets),
                IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return 0;
        TransferBridges.undoOnAbort(transaction, () -> handler.fill(drained, IFluidHandler.FluidAction.EXECUTE));
        return drained.getAmount() * DROPLETS_PER_MB;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        List<StorageView<FluidVariant>> views = new ArrayList<>(handler.getTanks());
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            views.add(new TankView(tank));
        }
        return views.iterator();
    }

    private final class TankView implements StorageView<FluidVariant> {

        private final int tank;

        TankView(int tank) {
            this.tank = tank;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            FluidStack inTank = handler.getFluidInTank(tank);
            if (inTank.isEmpty() || !resource.equals(FabricFluidHandler.variant(inTank))) return 0;
            return GTFluidStorage.this.extract(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getFluidInTank(tank).isEmpty();
        }

        @Override
        public FluidVariant getResource() {
            FluidStack inTank = handler.getFluidInTank(tank);
            return inTank.isEmpty() ? FluidVariant.blank() : FabricFluidHandler.variant(inTank);
        }

        @Override
        public long getAmount() {
            return handler.getFluidInTank(tank).getAmount() * DROPLETS_PER_MB;
        }

        @Override
        public long getCapacity() {
            return handler.getTankCapacity(tank) * DROPLETS_PER_MB;
        }
    }
}
