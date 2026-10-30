package com.gregtechceu.gtceu.compat.fluids;

import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

public interface IFluidTank {

    FluidStack getFluid();

    int getFluidAmount();

    int getCapacity();

    boolean isFluidValid(FluidStack stack);

    int fill(FluidStack resource, IFluidHandler.FluidAction action);

    FluidStack drain(int maxDrain, IFluidHandler.FluidAction action);

    FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action);
}
