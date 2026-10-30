package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

public final class DifferenceFluidIngredient extends FluidIngredient {

    public static final MapCodec<DifferenceFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FluidIngredient.CODEC_NON_EMPTY.fieldOf("base").forGetter(DifferenceFluidIngredient::base),
            FluidIngredient.CODEC_NON_EMPTY.fieldOf("subtracted").forGetter(DifferenceFluidIngredient::subtracted))
            .apply(i, DifferenceFluidIngredient::new));

    private final FluidIngredient base, subtracted;

    public DifferenceFluidIngredient(FluidIngredient base, FluidIngredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    public static FluidIngredient of(FluidIngredient base, FluidIngredient subtracted) {
        return new DifferenceFluidIngredient(base, subtracted);
    }

    public FluidIngredient base() {
        return base;
    }

    public FluidIngredient subtracted() {
        return subtracted;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return base.test(fluidStack) && !subtracted.test(fluidStack);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(base.getStacks()).filter(this);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * base.hashCode() + subtracted.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DifferenceFluidIngredient other && other.base.equals(base) && other.subtracted.equals(subtracted);
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.DIFFERENCE;
    }
}
