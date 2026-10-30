package com.gregtechceu.gtceu.compat.client.extensions.common;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.Nullable;

/** Client-side fluid appearance, registered per {@link FluidType} through {@link RegisterClientExtensionsEvent}. */
public interface IClientFluidTypeExtensions {

    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

    static IClientFluidTypeExtensions of(FluidType type) {
        return ClientExtensionsManager.FLUID_TYPE_EXTENSIONS.getOrDefault(type, DEFAULT);
    }

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return of(FluidType.of(fluid));
    }

    static IClientFluidTypeExtensions of(FluidState state) {
        return of(state.getType());
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    default @Nullable Identifier getStillTexture() {
        return null;
    }

    default @Nullable Identifier getFlowingTexture() {
        return null;
    }

    default @Nullable Identifier getOverlayTexture() {
        return null;
    }

    default @Nullable Identifier getRenderOverlayTexture(net.minecraft.client.Minecraft mc) {
        return null;
    }

    default int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getTintColor();
    }

    default int getTintColor(FluidStack stack) {
        return getTintColor();
    }

    default @Nullable Identifier getStillTexture(FluidStack stack) {
        return getStillTexture();
    }

    default @Nullable Identifier getFlowingTexture(FluidStack stack) {
        return getFlowingTexture();
    }

    default @Nullable Identifier getOverlayTexture(FluidStack stack) {
        return getOverlayTexture();
    }

    default @Nullable Identifier getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getStillTexture();
    }

    default @Nullable Identifier getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getFlowingTexture();
    }
}
