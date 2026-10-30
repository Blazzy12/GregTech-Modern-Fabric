package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fabric fluid storage seen as a GT fluid handler. GT counts millibuckets, Fabric droplets; only whole
 * millibuckets move.
 */
public final class FabricFluidHandler implements IFluidHandler {

    static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000;

    private final Storage<FluidVariant> storage;

    FabricFluidHandler(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    private List<StorageView<FluidVariant>> views() {
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        storage.forEach(views::add);
        return views;
    }

    static FluidVariant variant(FluidStack stack) {
        return FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
    }

    static FluidStack stack(FluidVariant variant, long millibuckets) {
        return new FluidStack(variant.getFluid(), (int) Math.min(Integer.MAX_VALUE, millibuckets),
                variant.getComponentsPatch());
    }

    @Override
    public int getTanks() {
        return Math.max(1, views().size());
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        List<StorageView<FluidVariant>> views = views();
        if (tank < 0 || tank >= views.size() || views.get(tank).isResourceBlank()) return FluidStack.EMPTY;
        return stack(views.get(tank).getResource(), views.get(tank).getAmount() / DROPLETS_PER_MB);
    }

    @Override
    public int getTankCapacity(int tank) {
        List<StorageView<FluidVariant>> views = views();
        if (tank < 0 || tank >= views.size()) return 0;
        return (int) Math.min(Integer.MAX_VALUE, views.get(tank).getCapacity() / DROPLETS_PER_MB);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !storage.supportsInsertion()) return 0;
        FluidVariant variant = variant(resource);
        long max = resource.getAmount() * DROPLETS_PER_MB;
        long whole;
        try (Transaction transaction = TransferBridges.open()) {
            whole = storage.insert(variant, max, transaction) / DROPLETS_PER_MB * DROPLETS_PER_MB;
        }
        if (whole == 0) return 0;
        if (action.execute()) {
            try (Transaction transaction = TransferBridges.open()) {
                whole = storage.insert(variant, whole, transaction);
                transaction.commit();
            }
        }
        return (int) (whole / DROPLETS_PER_MB);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        return drain(variant(resource), resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (StorageView<FluidVariant> view : views()) {
            if (!view.isResourceBlank() && view.getAmount() >= DROPLETS_PER_MB) {
                return drain(view.getResource(), maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    private FluidStack drain(FluidVariant variant, int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || !storage.supportsExtraction()) return FluidStack.EMPTY;
        long max = maxDrain * DROPLETS_PER_MB;
        long whole;
        try (Transaction transaction = TransferBridges.open()) {
            whole = storage.extract(variant, max, transaction) / DROPLETS_PER_MB * DROPLETS_PER_MB;
        }
        if (whole == 0) return FluidStack.EMPTY;
        if (action.execute()) {
            try (Transaction transaction = TransferBridges.open()) {
                whole = storage.extract(variant, whole, transaction);
                transaction.commit();
            }
        }
        return stack(variant, whole / DROPLETS_PER_MB);
    }
}
