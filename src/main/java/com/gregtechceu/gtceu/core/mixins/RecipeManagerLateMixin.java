package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Builds GT's recipe lookups once a new recipe manager has its recipes (child recipes: {@link RecipeMapMixin}). In
 * 26.x that is when the server finalises recipe loading: tags, which the lookups read, are only bound by then.
 */
@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin {

    @Shadow
    @Final
    private RecipeMap recipes;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    private void gtceu$buildRecipeLookups(FeatureFlagSet enabledFlags, CallbackInfo ci) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                continue;
            }
            gtRecipeType.beginStagingRecipes();
            gtRecipeType.getProxyRecipes().forEach((type, list) -> {
                Collection<RecipeHolder<?>> proxied = new ArrayList<>(this.recipes.byType((RecipeType) type));
                if (proxied.isEmpty()) {
                    return;
                }
                RecipeManagerHandler.addProxyRecipesToLookup(proxied, gtRecipeType, type, list);
            });
            Collection<RecipeHolder<?>> recipesByID = new ArrayList<>(this.recipes.byType((RecipeType) gtRecipeType));
            RecipeManagerHandler.addRecipesToLookup(recipesByID, gtRecipeType);
            gtRecipeType.getAdditionHandler().completeStaging();
        }
        MapIngredientPool.clear();
    }
}
