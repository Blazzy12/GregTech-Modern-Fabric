package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import com.mojang.serialization.MapCodec;

import java.util.List;
import java.util.stream.Stream;

/** Matches any of its children. */
public final class CompoundFluidIngredient extends FluidIngredient {

    public static final MapCodec<CompoundFluidIngredient> CODEC = FluidIngredient.LIST_CODEC_NON_EMPTY
            .xmap(CompoundFluidIngredient::new, CompoundFluidIngredient::children).fieldOf("ingredients");

    private final List<FluidIngredient> children;

    private CompoundFluidIngredient(List<FluidIngredient> children) {
        this.children = List.copyOf(children);
    }

    public static FluidIngredient of(List<FluidIngredient> children) {
        if (children.isEmpty()) return empty();
        if (children.size() == 1) return children.getFirst();
        return new CompoundFluidIngredient(children);
    }

    public List<FluidIngredient> children() {
        return children;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        for (FluidIngredient child : children) {
            if (child.test(fluidStack)) return true;
        }
        return false;
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return children.stream().flatMap(child -> Stream.of(child.getStacks()));
    }

    @Override
    public boolean isSimple() {
        return children.stream().allMatch(FluidIngredient::isSimple);
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.COMPOUND;
    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompoundFluidIngredient other && other.children.equals(children);
    }
}
