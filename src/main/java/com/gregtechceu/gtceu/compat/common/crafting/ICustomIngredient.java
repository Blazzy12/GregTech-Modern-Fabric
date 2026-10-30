package com.gregtechceu.gtceu.compat.common.crafting;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.stream.Stream;

/**
 * NeoForge-style custom ingredient on top of Fabric's {@link CustomIngredient}.
 * Serialization goes through the {@link IngredientType} registered in {@code NeoForgeRegistries.INGREDIENT_TYPES}.
 */
public interface ICustomIngredient extends CustomIngredient {

    @Override
    boolean test(ItemStack stack);

    /** Every stack this ingredient can match, used for display. */
    Stream<ItemStack> getItems();

    /** Simple ingredients match on item alone, without inspecting components. */
    boolean isSimple();

    IngredientType<?> getType();

    @Override
    default Stream<Holder<Item>> items() {
        return getItems().map(ItemStack::typeHolder).distinct();
    }

    @Override
    default boolean requiresTesting() {
        return !isSimple();
    }

    @Override
    default CustomIngredientSerializer<?> getSerializer() {
        return IngredientType.serializerFor(getType());
    }

    @Override
    default Ingredient toVanilla() {
        return CustomIngredient.super.toVanilla();
    }
}
