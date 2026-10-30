package com.gregtechceu.gtceu.compat.fluids.capability.templates;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

/** Accepts and destroys any fluid. */
public class VoidFluidHandler implements IFluidHandler {

    public static final VoidFluidHandler INSTANCE = new VoidFluidHandler();

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return resource.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
