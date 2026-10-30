package com.gregtechceu.gtceu.data.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * A {@link RecipeOutput} for GT's runtime recipe generation. 26.x recipe outputs also act as a registry lookup and
 * take {@link ResourceKey} ids; this keeps GT's id-based {@link #accept(Identifier, Recipe, AdvancementHolder)}.
 */
public abstract class GTRecipeOutput implements RecipeOutput {

    private final net.minecraft.data.worldgen.BootstrapContextAccess registries;

    protected GTRecipeOutput(HolderLookup.Provider registries) {
        this.registries = new net.minecraft.data.worldgen.BootstrapContextAccess() {

            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                return registries.lookupOrThrow(key);
            }

            @Override
            @SuppressWarnings("deprecation")
            public <S> Stream<Holder.Reference<S>> listContextElements(
                                                                       ResourceKey<? extends Registry<? extends S>> key) {
                return registries.lookupOrThrow(key).listElements();
            }
        };
    }

    /** An output with no registry lookups, for places that have none (the recipe map is built before them). */
    protected GTRecipeOutput() {
        this.registries = new net.minecraft.data.worldgen.BootstrapContextAccess() {

            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                throw new UnsupportedOperationException("No registry lookups here");
            }

            @Override
            public <S> Stream<Holder.Reference<S>> listContextElements(
                                                                       ResourceKey<? extends Registry<? extends S>> key) {
                throw new UnsupportedOperationException("No registry lookups here");
            }
        };
    }

    /** Wraps another output, sharing its registry lookups. */
    protected GTRecipeOutput(RecipeOutput delegate) {
        this.registries = delegate;
    }

    public abstract void accept(Identifier id, Recipe<?> recipe, @Nullable AdvancementHolder advancement);

    @Override
    public final void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
        accept(id.identifier(), recipe, advancement);
    }

    /** Sends a recipe to a vanilla output by id. */
    public static void accept(RecipeOutput output, Identifier id, Recipe<?> recipe,
                              @Nullable AdvancementHolder advancement) {
        if (output instanceof GTRecipeOutput gt) {
            gt.accept(id, recipe, advancement);
        } else {
            output.accept(ResourceKey.create(Registries.RECIPE, id), recipe, advancement);
        }
    }

    @Override
    @SuppressWarnings("removal")
    public Advancement.Builder advancement() {
        return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
    }

    @Override
    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        // recipes are no registry outside of datagen; unlock advancements only need a holder for the recipe's key
        if (key.equals(Registries.RECIPE)) {
            return com.gregtechceu.gtceu.data.pack.KeyOnlyLookups.get(key);
        }
        return registries.lookup(key);
    }



    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return registries.listContextElements(key);
    }
}
