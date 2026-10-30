package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import com.gregtechceu.gtceu.compat.common.crafting.DataComponentIngredient;
import com.gregtechceu.gtceu.compat.common.crafting.IngredientType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Data component ingredient that clears the item cache when tags are reloaded.
 *
 * @see DataComponentIngredient DataComponentIngredient, the parent class
 * @see ExDataComponentFluidIngredient ExDataComponentFluidIngredient, its fluid equivalent
 */
public class ExDataComponentIngredient extends DataComponentIngredient {

    // spotless:off
    public static final MapCodec<ExDataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(ExDataComponentIngredient::itemSet),
            DataComponentExactPredicate.CODEC.fieldOf("components").forGetter(ExDataComponentIngredient::components),
            Codec.BOOL.optionalFieldOf("strict", false).forGetter(ExDataComponentIngredient::isStrict)
    ).apply(builder, ExDataComponentIngredient::new));
    // spotless:on

    /** Bumped on every tag reload; NeoForge holder sets had invalidation listeners instead. */
    private static volatile int tagGeneration = 0;

    static {
        net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TAGS_LOADED
                .register((registries, client) -> tagGeneration++);
    }

    private final @NotNull List<ItemStack> stacks = new ArrayList<>();
    private int stacksGeneration = -1;

    public ExDataComponentIngredient(HolderSet<Item> items, DataComponentExactPredicate components, boolean strict) {
        super(items, components, strict);
    }

    private synchronized List<ItemStack> regenerateStacksIfEmpty() {
        if (this.stacksGeneration != tagGeneration) {
            this.stacks.clear();
            this.itemSet().stream()
                    .map(i -> new ItemStack(i, 1, components().asPatch()))
                    .forEach(this.stacks::add);
            this.stacksGeneration = tagGeneration;
        }
        return this.stacks;
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (this.isStrict()) {
            for (ItemStack stack2 : this.regenerateStacksIfEmpty()) {
                if (ItemStack.isSameItemSameComponents(stack, stack2)) return true;
            }
            return false;
        } else {
            return this.itemSet().contains(stack.typeHolder()) && this.components().test(stack);
        }
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return this.regenerateStacksIfEmpty().stream();
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return GTIngredientTypes.DATA_COMPONENT_INGREDIENT.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExDataComponentIngredient)) return false;
        return super.equals(obj);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return of(strict, DataComponentExactPredicate.allOf(map), items);
    }

    /**
     * Creates a new ingredient matching any item in the tag, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentExactPredicate predicate, TagKey<Item> tag) {
        return of(strict, predicate, com.gregtechceu.gtceu.utils.TagUtil.holderSet(BuiltInRegistries.ITEM, tag));
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentExactPredicate predicate, HolderSet<Item> items) {
        return new ExDataComponentIngredient(items, predicate, strict).toVanilla();
    }
}
