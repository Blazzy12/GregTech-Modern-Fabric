package com.gregtechceu.gtceu.compat.transfer;

import com.gregtechceu.gtceu.compat.client.extensions.common.IClientFluidTypeExtensions;
import com.gregtechceu.gtceu.compat.fluids.BaseFlowingFluid;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import org.jetbrains.annotations.Nullable;

/** Fluid colours for Fabric's fluid rendering API, from GT's per-stack tints (e.g. potion fluids). */
@Environment(EnvType.CLIENT)
public final class FluidVariantRenderHandlers {

    private FluidVariantRenderHandlers() {}

    public static void init() {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid instanceof BaseFlowingFluid) {
                FluidVariantRendering.register(fluid, Handler.INSTANCE);
            }
        }
    }

    private enum Handler implements FluidVariantRenderHandler {

        INSTANCE;

        @Override
        public int getColor(FluidVariant variant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
            var ext = IClientFluidTypeExtensions.of(variant.getFluid());
            if (view != null && pos != null) {
                return ext.getTintColor(variant.getFluid().defaultFluidState(), view, pos);
            }
            return ext.getTintColor(FluidVariantHandlers.toStack(variant));
        }
    }
}
