package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.FacadeCoverRecipe;
import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;
import com.gregtechceu.gtceu.api.recipe.ShapedFluidContainerRecipe;
import com.gregtechceu.gtceu.api.recipe.StrictShapedRecipe;
import com.gregtechceu.gtceu.api.recipe.ToolHeadReplaceRecipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class GTRecipeSerializers {

    public static final RecipeSerializer<StrictShapedRecipe> CRAFTING_SHAPED_STRICT = StrictShapedRecipe.SERIALIZER;
    public static final RecipeSerializer<ShapedEnergyTransferRecipe> CRAFTING_SHAPED_ENERGY_TRANSFER = ShapedEnergyTransferRecipe.SERIALIZER;
    public static final RecipeSerializer<ShapedFluidContainerRecipe> CRAFTING_SHAPED_FLUID_CONTAINER = ShapedFluidContainerRecipe.SERIALIZER;
    public static final RecipeSerializer<ToolHeadReplaceRecipe> CRAFTING_TOOL_HEAD_REPLACE = ToolHeadReplaceRecipe.SERIALIZER;
    public static final RecipeSerializer<FacadeCoverRecipe> CRAFTING_FACADE_COVER = FacadeCoverRecipe.SERIALIZER;

    public static void init() {
        register("crafting_shaped_strict", CRAFTING_SHAPED_STRICT);
        register("crafting_shaped_energy_transfer", CRAFTING_SHAPED_ENERGY_TRANSFER);
        register("crafting_shaped_fluid_container", CRAFTING_SHAPED_FLUID_CONTAINER);
        register("crafting_tool_head_replace", CRAFTING_TOOL_HEAD_REPLACE);
        register("crafting_facade_cover", CRAFTING_FACADE_COVER);
    }

    private static void register(String name, RecipeSerializer<?> serializer) {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, GTCEu.id(name), serializer);
    }
}
