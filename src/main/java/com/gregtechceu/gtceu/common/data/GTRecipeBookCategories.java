package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;

/** Every recipe must name a recipe book category in 26.x; GT machine recipes never appear in the book. */
public final class GTRecipeBookCategories {

    public static final RecipeBookCategory MACHINE = Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY,
            GTCEu.id("machine"), new RecipeBookCategory());

    private GTRecipeBookCategories() {}

    public static void init() {}
}
