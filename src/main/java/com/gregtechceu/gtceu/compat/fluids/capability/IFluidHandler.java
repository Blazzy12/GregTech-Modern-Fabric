package com.gregtechceu.gtceu.compat.fluids.capability;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

public interface IFluidHandler {

    enum FluidAction {

        EXECUTE,
        SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }
    }

    int getTanks();

    FluidStack getFluidInTank(int tank);

    int getTankCapacity(int tank);

    boolean isFluidValid(int tank, FluidStack stack);

    /** @return the amount filled */
    int fill(FluidStack resource, FluidAction action);

    FluidStack drain(FluidStack resource, FluidAction action);

    FluidStack drain(int maxDrain, FluidAction action);
}
