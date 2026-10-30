package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.common.crafting.ICustomIngredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Injected into {@link Ingredient}: the 1.21.1 accessors GT still uses. */
public interface IngredientExt {

    private Ingredient self() {
        return (Ingredient) this;
    }

    /** Every matching stack. Custom ingredients supply their own stacks, including components. */
    default ItemStack[] getItems() {
        CustomIngredient custom = self().getCustomIngredient();
        if (custom instanceof ICustomIngredient neo) return neo.getItems().toArray(ItemStack[]::new);
        return self().items().map(ItemStack::new).toArray(ItemStack[]::new);
    }

    default boolean isCustom() {
        return self().getCustomIngredient() != null;
    }

    default boolean hasNoItems() {
        return self().isEmpty() || getItems().length == 0;
    }

    default boolean isSimple() {
        return !self().requiresTesting();
    }

    /** The holder set this ingredient matches (a tag, or a direct list of items). */
    default net.minecraft.core.HolderSet<net.minecraft.world.item.Item> getValueSet() {
        return self().values;
    }

    /** The tag this ingredient was built from, if it is a plain tag ingredient (1.21.1's {@code TagValue}). */
    default java.util.Optional<net.minecraft.tags.TagKey<net.minecraft.world.item.Item>> getTag() {
        if (isCustom()) return java.util.Optional.empty();
        return self().values.unwrapKey();
    }
}
