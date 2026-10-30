package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.MapCodec;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TagFluidIngredient extends FluidIngredient {

    public static final MapCodec<TagFluidIngredient> CODEC = TagKey.codec(Registries.FLUID)
            .xmap(TagFluidIngredient::new, TagFluidIngredient::tag).fieldOf("tag");

    private final TagKey<Fluid> tag;

    public TagFluidIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(tag);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value)
                .filter(fluid -> fluid.isSource(fluid.defaultFluidState()))
                .map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return FluidIngredientType.TAG;
    }

    public TagKey<Fluid> tag() {
        return tag;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TagFluidIngredient other && other.tag.equals(tag);
    }
}
