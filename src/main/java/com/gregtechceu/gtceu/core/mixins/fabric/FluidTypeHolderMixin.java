package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.FluidTypeHolder;
import net.minecraft.world.level.material.Fluid;

import org.spongepowered.asm.mixin.Mixin;

/** Makes the injected {@link FluidTypeHolder} interface present at runtime. */
@Mixin(Fluid.class)
public abstract class FluidTypeHolderMixin implements FluidTypeHolder {}
