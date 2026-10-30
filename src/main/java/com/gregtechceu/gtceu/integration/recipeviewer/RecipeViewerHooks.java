package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entry points that the active recipe viewer plugin installs, so core code never references a viewer's classes.
 */
public final class RecipeViewerHooks {

    /** Opens the viewer on the given category. No-op when no viewer is installed. */
    public static Consumer<GTRecipeCategory> categoryOpener = category -> {};

    /** Wraps a category icon into the viewer's renderable type, or null when no viewer is installed. */
    public static Function<Either<Identifier, ItemStack>, @Nullable Object> iconFactory = texture -> null;

    private RecipeViewerHooks() {}
}
