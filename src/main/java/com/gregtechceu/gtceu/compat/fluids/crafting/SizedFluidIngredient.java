package com.gregtechceu.gtceu.compat.fluids.crafting;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

/** A fluid ingredient together with the amount it requires. */
public final class SizedFluidIngredient {

    public static final Codec<SizedFluidIngredient> FLAT_CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1000).forGetter(SizedFluidIngredient::amount))
            .apply(i, SizedFluidIngredient::new));

    public static final Codec<SizedFluidIngredient> NESTED_CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1000).forGetter(SizedFluidIngredient::amount))
            .apply(i, SizedFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec
            .composite(
                    FluidIngredient.STREAM_CODEC, SizedFluidIngredient::ingredient,
                    ByteBufCodecs.VAR_INT, SizedFluidIngredient::amount,
                    SizedFluidIngredient::new);

    private final FluidIngredient ingredient;
    private final int amount;
    private FluidStack[] cachedStacks;

    public SizedFluidIngredient(FluidIngredient ingredient, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive: " + amount);
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        return new SizedFluidIngredient(FluidIngredient.single(fluid), amount);
    }

    public static SizedFluidIngredient of(FluidStack stack) {
        return new SizedFluidIngredient(FluidIngredient.single(stack), stack.getAmount());
    }

    public static SizedFluidIngredient of(TagKey<Fluid> tag, int amount) {
        return new SizedFluidIngredient(FluidIngredient.tag(tag), amount);
    }

    public FluidIngredient ingredient() {
        return ingredient;
    }

    public int amount() {
        return amount;
    }

    public boolean test(FluidStack stack) {
        return ingredient.test(stack) && stack.getAmount() >= amount;
    }

    public FluidStack[] getFluids() {
        if (cachedStacks == null) {
            FluidStack[] base = ingredient.getStacks();
            cachedStacks = new FluidStack[base.length];
            for (int i = 0; i < base.length; i++) cachedStacks[i] = base[i].copyWithAmount(amount);
        }
        return cachedStacks;
    }

    public SizedFluidIngredient shrink(int amount) {
        return com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.shrink(this, amount);
    }

    public SizedFluidIngredient copy() {
        return com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.copy(this);
    }

    public SizedFluidIngredient copyWithAmount(int amount) {
        return com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.copyWithAmount(this, amount);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SizedFluidIngredient other && amount == other.amount &&
                ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount);
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
