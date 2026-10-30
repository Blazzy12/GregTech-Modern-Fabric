package com.tterrag.registrate.util;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.ObjectArrays;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * A helper for data generation when using ingredients as input(s) to recipes.<br>
 * It remembers the name of the primary ingredient for use in creating recipe names.
 * <p>
 * Datagen does not run on Fabric, so the advancement criteria of the original are gone.
 */
public final class DataIngredient {

    private final Ingredient parent;
    private final Identifier id;

    private DataIngredient(Ingredient parent, Identifier id) {
        this.parent = parent;
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(NonNullSupplier<? extends T> first,
                                                            NonNullSupplier<? extends T>... others) {
        return items(first.get(), (T[]) Arrays.stream(others).map(Supplier::get).toArray(ItemLike[]::new));
    }

    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(T first, T... others) {
        return ingredient(Ingredient.of(ObjectArrays.concat(first, others)), first);
    }

    public static DataIngredient stacks(ItemStack first, ItemStack... others) {
        return ingredient(Ingredient.of(Arrays.stream(ObjectArrays.concat(first, others)).map(ItemStack::getItem)),
                first.getItem());
    }

    public static DataIngredient tag(TagKey<Item> tag) {
        return ingredient(Ingredient.of(com.gregtechceu.gtceu.utils.TagUtil.holderSet(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag)), tag);
    }

    public static DataIngredient ingredient(Ingredient parent, ItemLike required) {
        return new DataIngredient(parent, BuiltInRegistries.ITEM.getKey(required.asItem()));
    }

    public static DataIngredient ingredient(Ingredient parent, TagKey<Item> required) {
        return new DataIngredient(parent, required.location());
    }

    public static DataIngredient ingredient(Ingredient parent, Identifier id) {
        return new DataIngredient(parent, id);
    }

    public Ingredient toVanilla() {
        return parent;
    }

    public Identifier getId() {
        return this.id;
    }

    public boolean test(ItemStack stack) {
        return this.parent.test(stack);
    }

    public boolean isEmpty() {
        return this.parent.isEmpty();
    }
}
