package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.stream.Stream;

public class SingleFluidIngredient extends FluidIngredient {

    public static final MapCodec<SingleFluidIngredient> CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
            .validate(fluid -> fluid.is(Fluids.EMPTY.builtInRegistryHolder()) ?
                    DataResult.error(() -> "Fluid must not be empty") : DataResult.success(fluid))
            .xmap(SingleFluidIngredient::new, SingleFluidIngredient::fluid).fieldOf("fluid");

    private final Holder<Fluid> fluid;

    public SingleFluidIngredient(Holder<Fluid> fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluid);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.SINGLE;
    }

    public Holder<Fluid> fluid() {
        return fluid;
    }

    @Override
    public int hashCode() {
        return fluid.value().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SingleFluidIngredient other && other.fluid.value() == fluid.value();
    }
}
