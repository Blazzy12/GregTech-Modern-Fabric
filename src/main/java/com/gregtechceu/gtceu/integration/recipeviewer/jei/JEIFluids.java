package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;

/**
 * JEI on Fabric shows fluids as {@link IJeiFluidIngredient}s (a {@link FluidVariant} and an amount in droplets),
 * where GT has its millibucket {@link FluidStack}s.
 */
public final class JEIFluids {

    /** Droplets in a millibucket. */
    public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000;

    private JEIFluids() {}

    public static IJeiFluidIngredient of(FluidStack stack) {
        return new Ingredient(FluidVariant.of(stack.getFluid(), stack.getComponentsPatch()),
                stack.getAmount() * DROPLETS_PER_MB);
    }

    public static FluidStack toGT(IJeiFluidIngredient ingredient) {
        FluidVariant variant = ingredient.getFluidVariant();
        return new FluidStack(variant.getFluid(), (int) (ingredient.getAmount() / DROPLETS_PER_MB),
                variant.getComponentsPatch());
    }

    private record Ingredient(FluidVariant getFluidVariant, long getAmount) implements IJeiFluidIngredient {}
}
