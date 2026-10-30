package com.gregtechceu.gtceu.compat.common.crafting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

/** An ingredient together with the number of items it requires. */
public final class SizedIngredient {

    public static final Codec<SizedIngredient> FLAT_CODEC = RecordCodecBuilder.create(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SizedIngredient::ingredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count))
            .apply(i, SizedIngredient::new));

    public static final Codec<SizedIngredient> NESTED_CODEC = FLAT_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, SizedIngredient::ingredient,
            ByteBufCodecs.VAR_INT, SizedIngredient::count,
            SizedIngredient::new);

    private final Ingredient ingredient;
    private final int count;
    private ItemStack[] cachedStacks;

    public SizedIngredient(Ingredient ingredient, int count) {
        if (count <= 0) throw new IllegalArgumentException("Count must be positive: " + count);
        this.ingredient = ingredient;
        this.count = count;
    }

    public static SizedIngredient of(ItemLike item, int count) {
        return new SizedIngredient(Ingredient.of(item), count);
    }

    public static SizedIngredient of(TagKey<Item> tag, int count) {
        return new SizedIngredient(Ingredient.of(com.gregtechceu.gtceu.utils.TagUtil.holderSet(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag)), count);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int count() {
        return count;
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    public ItemStack[] getItems() {
        if (cachedStacks == null) {
            cachedStacks = ingredient.getItems().clone();
            for (int i = 0; i < cachedStacks.length; i++) cachedStacks[i] = cachedStacks[i].copyWithCount(count);
        }
        return cachedStacks;
    }

    public @org.jetbrains.annotations.Nullable net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient getContainedCustom() {
        return ingredient.getCustomIngredient();
    }

    public SizedIngredient shrink(int count) {
        return com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.shrink(this, count);
    }

    public SizedIngredient copy() {
        return com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.copy(this);
    }

    public SizedIngredient copyWithCount(int count) {
        return com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.copyWithCount(this, count);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SizedIngredient other && count == other.count && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, count);
    }

    @Override
    public String toString() {
        return count + "x " + ingredient;
    }
}
