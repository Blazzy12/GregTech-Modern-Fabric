package brachy.modularui.utils.handlers.fluid;

import com.gregtechceu.gtceu.compat.fluids.IFluidTank;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

public interface IMultiTankFluidHandler extends IFluidHandler {

    IFluidTank getFluidTank(int index);
}
