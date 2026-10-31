package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.fluids.BaseFlowingFluid;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

/**
 * Tells Fabric's fluid attribute API (which JEI, Jade and other mods read) about GT fluids. Without it Fabric names a
 * fluid after its block's translation key, so GT fluids, most of which have no block, showed as
 * {@code block.gtceu.<name>}.
 */
public final class FluidVariantHandlers {

    private FluidVariantHandlers() {}

    /** Registers a handler for every fluid with its own {@link FluidType}. Call after all fluids are registered. */
    public static void init() {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid instanceof BaseFlowingFluid) {
                FluidVariantAttributes.register(fluid, Handler.INSTANCE);
            }
        }
    }

    static FluidStack toStack(FluidVariant variant) {
        return new FluidStack(variant.getFluid(), 1000, variant.getComponentsPatch());
    }

    private enum Handler implements FluidVariantAttributeHandler {

        INSTANCE;

        @Override
        public Component getName(FluidVariant variant) {
            FluidStack stack = toStack(variant);
            return FluidType.of(variant.getFluid()).getDescription(stack);
        }

        @Override
        public int getLightEmission(FluidVariant variant) {
            return FluidType.of(variant.getFluid()).getLightLevel(toStack(variant));
        }

        @Override
        public int getTemperature(FluidVariant variant) {
            return FluidType.of(variant.getFluid()).getTemperature(toStack(variant));
        }

        @Override
        public int getViscosity(FluidVariant variant, Level level) {
            return FluidType.of(variant.getFluid()).getViscosity(toStack(variant));
        }

        @Override
        public boolean isLighterThanAir(FluidVariant variant) {
            return FluidType.of(variant.getFluid()).getDensity(toStack(variant)) <= 0;
        }
    }
}
