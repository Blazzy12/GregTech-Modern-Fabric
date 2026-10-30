package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Matches fluids that carry the given data components, exactly when strict. */
public class DataComponentFluidIngredient extends FluidIngredient {

    public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false)
                    .fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluids),
            DataComponentExactPredicate.CODEC.fieldOf("components")
                    .forGetter(DataComponentFluidIngredient::components),
            Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict))
            .apply(i, DataComponentFluidIngredient::new));

    private final HolderSet<Fluid> fluids;
    private final DataComponentExactPredicate components;
    private final boolean strict;
    private final FluidStack[] stacks;

    public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentExactPredicate components,
                                        boolean strict) {
        this.fluids = fluids;
        this.components = components;
        this.strict = strict;
        this.stacks = fluids.stream()
                .map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME, components.asPatch()))
                .toArray(FluidStack[]::new);
    }

    @Override
    public boolean test(FluidStack stack) {
        if (!fluids.contains(stack.getFluidHolder())) return false;
        if (strict) {
            for (FluidStack candidate : stacks) {
                if (FluidStack.isSameFluidSameComponents(stack, candidate)) return true;
            }
            return false;
        }
        return components.test(stack);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Arrays.stream(stacks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.DATA_COMPONENT;
    }

    public HolderSet<Fluid> fluids() {
        return fluids;
    }

    public DataComponentExactPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluids, components, strict);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataComponentFluidIngredient other && other.fluids.equals(fluids) &&
                other.components.equals(components) && other.strict == strict;
    }

    public static FluidIngredient of(boolean strict, FluidStack stack) {
        return of(strict, stack.getComponents(), stack.getFluid());
    }

    public static <T> FluidIngredient of(boolean strict, DataComponentType<? super T> type, T value,
                                         Fluid... fluids) {
        return of(strict, DataComponentExactPredicate.builder().expect(type, value).build(), fluids);
    }

    public static <T> FluidIngredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type,
                                         T value, Fluid... fluids) {
        return of(strict, type.get(), value, fluids);
    }

    public static FluidIngredient of(boolean strict, DataComponentMap map, Fluid... fluids) {
        return of(strict, DataComponentExactPredicate.allOf(map), fluids);
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentMap map, Holder<Fluid>... fluids) {
        return of(strict, DataComponentExactPredicate.allOf(map), HolderSet.direct(fluids));
    }

    public static FluidIngredient of(boolean strict, DataComponentMap map, HolderSet<Fluid> fluids) {
        return of(strict, DataComponentExactPredicate.allOf(map), fluids);
    }

    public static FluidIngredient of(boolean strict, DataComponentExactPredicate predicate, Fluid... fluids) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(fluids).map(Fluid::builtInRegistryHolder)
                .toList()));
    }

    public static FluidIngredient of(boolean strict, DataComponentExactPredicate predicate,
                                     HolderSet<Fluid> fluids) {
        return new DataComponentFluidIngredient(fluids, predicate, strict);
    }
}
