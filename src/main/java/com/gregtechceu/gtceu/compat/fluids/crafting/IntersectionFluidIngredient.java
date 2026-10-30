package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import com.mojang.serialization.MapCodec;
import java.util.List;

import java.util.stream.Stream;

public final class IntersectionFluidIngredient extends FluidIngredient {

    public static final MapCodec<IntersectionFluidIngredient> CODEC = FluidIngredient.LIST_CODEC_NON_EMPTY
            .xmap(IntersectionFluidIngredient::new, IntersectionFluidIngredient::children).fieldOf("children");

    private final List<FluidIngredient> children;

    public IntersectionFluidIngredient(List<FluidIngredient> children) {
        this.children = List.copyOf(children);
    }

    public static FluidIngredient of(FluidIngredient... children) {
        return new IntersectionFluidIngredient(List.of(children));
    }

    public List<FluidIngredient> children() {
        return children;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        for (FluidIngredient child : children) {
            if (!child.test(fluidStack)) return false;
        }
        return true;
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(children.getFirst().getStacks()).filter(this);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntersectionFluidIngredient other && other.children.equals(children);
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.INTERSECTION;
    }
}
