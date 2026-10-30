package com.gregtechceu.gtceu.compat.common.crafting;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Matches items that carry the given data components, exactly when strict. */
public class DataComponentIngredient implements ICustomIngredient {

    public static final MapCodec<DataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            HolderSetCodec.create(Registries.ITEM, Item.CODEC, false).fieldOf("items")
                    .forGetter(DataComponentIngredient::itemSet),
            DataComponentExactPredicate.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
            Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::isStrict))
            .apply(i, DataComponentIngredient::new));

    public static final IngredientType<DataComponentIngredient> TYPE = new IngredientType<>(CODEC);

    private final HolderSet<Item> items;
    private final DataComponentExactPredicate components;
    private final boolean strict;
    private final ItemStack[] stacks;

    public DataComponentIngredient(HolderSet<Item> items, DataComponentExactPredicate components, boolean strict) {
        this.items = items;
        this.components = components;
        this.strict = strict;
        this.stacks = items.stream().map(item -> {
            ItemStack stack = new ItemStack(item);
            stack.applyComponents(components.asPatch());
            return stack;
        }).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!items.contains(stack.typeHolder())) return false;
        if (strict) {
            for (ItemStack candidate : stacks) {
                if (ItemStack.isSameItemSameComponents(stack, candidate)) return true;
            }
            return false;
        }
        return components.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(stacks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    public HolderSet<Item> itemSet() {
        return items;
    }

    public DataComponentExactPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    public static Ingredient of(boolean strict, ItemStack stack) {
        return of(strict, stack.getComponents(), stack.getItem());
    }

    public static <T> Ingredient of(boolean strict, DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(strict, DataComponentExactPredicate.builder().expect(type, value).build(), items);
    }

    public static <T> Ingredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value,
                                    ItemLike... items) {
        return of(strict, type.get(), value, items);
    }

    public static Ingredient of(boolean strict, DataComponentMap map, ItemLike... items) {
        return of(strict, DataComponentExactPredicate.allOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, DataComponentMap map, Holder<Item>... items) {
        return of(strict, DataComponentExactPredicate.allOf(map), HolderSet.direct(items));
    }

    public static Ingredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return of(strict, DataComponentExactPredicate.allOf(map), items);
    }

    public static Ingredient of(boolean strict, DataComponentExactPredicate predicate, ItemLike... items) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(items)
                .map(item -> item.asItem().builtInRegistryHolder()).toList()));
    }

    public static Ingredient of(boolean strict, DataComponentExactPredicate predicate, HolderSet<Item> items) {
        return new DataComponentIngredient(items, predicate, strict).toVanilla();
    }
}
