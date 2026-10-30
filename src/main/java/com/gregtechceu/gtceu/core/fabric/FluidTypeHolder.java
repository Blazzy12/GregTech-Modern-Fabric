package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.minecraft.world.level.material.Fluid;

/**
 * Injected into {@link Fluid} through the class tweaker so every fluid has a {@link FluidType}.
 * Fluids that do not override it use the vanilla defaults.
 */
public interface FluidTypeHolder {

    default FluidType getFluidType() {
        return FluidType.defaultFor((Fluid) this);
    }
}
