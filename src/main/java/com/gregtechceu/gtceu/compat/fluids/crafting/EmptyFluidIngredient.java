package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import com.mojang.serialization.MapCodec;

import java.util.stream.Stream;

public final class EmptyFluidIngredient extends FluidIngredient {

    public static final EmptyFluidIngredient INSTANCE = new EmptyFluidIngredient();
    public static final MapCodec<EmptyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptyFluidIngredient() {}

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.isEmpty();
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.empty();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.EMPTY;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
