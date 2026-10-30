package com.gregtechceu.gtceu.api.transfer.fluid;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

public interface IFluidHandlerModifiable extends IFluidHandler {

    void setFluidInTank(int tank, FluidStack stack);

    default boolean supportsFill(int tank) {
        return true;
    }

    default boolean supportsDrain(int tank) {
        return true;
    }
}
