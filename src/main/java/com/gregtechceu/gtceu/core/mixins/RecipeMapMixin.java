package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.GTRecipeOutput;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Regenerates the child recipes of GT recipes (whatever their recipe type's {@code onSave} adds), including for
 * recipes that came from datapacks, and applies GT's recipe removals. 26.x builds the recipe map from the recipe
 * registry, so this hooks that.
 */
// a low priority puts this before other mods' hooks on create() (Fabric's recipe sync index), so they see this map
@Mixin(value = RecipeMap.class, priority = 500)
public abstract class RecipeMapMixin {

    @ModifyReturnValue(method = "create", at = @At("RETURN"))
    private static RecipeMap gtceu$addChildRecipes(RecipeMap original) {
        // use a linked hash map to keep the immutable map's order
        Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = new LinkedHashMap<>();
        original.values().forEach(holder -> byKey.put(holder.id(), holder));
        // GT's recipe removals (was a filter on the recipe JSON map before 26.x)
        com.gregtechceu.gtceu.common.data.GTRecipes
                .recipeRemoval(id -> byKey.remove(ResourceKey.create(Registries.RECIPE, id)));
        int sizeBefore = original.values().size();
        for (RecipeHolder<?> holder : original.values()) {
            if (holder.value() instanceof GTRecipe gtRecipe) {
                new GTRecipeBuilder(gtRecipe, gtRecipe.recipeType)
                        .id(holder.id().identifier().withPath(path -> path.substring(path.indexOf('/') + 1)))
                        .onSave(gtRecipe.recipeType.getRecipeBuilder().onSave)
                        .save(new GTRecipeOutput() {

                            @Override
                            public void accept(Identifier id, Recipe<?> recipe,
                                               @Nullable AdvancementHolder advancement) {
                                ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
                                byKey.put(key, new RecipeHolder<>(key, recipe));
                            }
                        });
            }
        }
        if (byKey.size() == sizeBefore && byKey.values().stream().allMatch(h -> original.byKey(h.id()) == h)) {
            return original;
        }
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        byKey.values().forEach(holder -> byType.put(holder.value().getType(), holder));
        Multimap<RecipeType<?>, RecipeHolder<?>> types = byType.build();
        return RecipeMapAccessor.gtceu$create(types, Map.copyOf(byKey));
    }
}
