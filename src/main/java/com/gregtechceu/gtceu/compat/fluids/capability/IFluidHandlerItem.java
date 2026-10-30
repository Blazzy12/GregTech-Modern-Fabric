package com.gregtechceu.gtceu.compat.fluids.capability;

import net.minecraft.world.item.ItemStack;

public interface IFluidHandlerItem extends IFluidHandler {

    /** The container after any changes; may differ from the stack the handler was created for. */
    ItemStack getContainer();
}
