package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** A shaped recipe that is never mirrored. */
public class StrictShapedRecipe extends GTShapedRecipe {

    public static final RecipeSerializer<StrictShapedRecipe> SERIALIZER = new RecipeSerializer<>(
            codec(StrictShapedRecipe::new), streamCodec(StrictShapedRecipe::new));

    public StrictShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                              ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        for (int i = 0; i <= inv.width() - this.getWidth(); ++i) {
            for (int j = 0; j <= inv.height() - this.getHeight(); ++j) {
                if (this.matches(inv, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean matches(CraftingInput craftingInventory, int width, int height) {
        for (int i = 0; i < craftingInventory.width(); ++i) {
            for (int j = 0; j < craftingInventory.height(); ++j) {
                int k = i - width;
                int l = j - height;
                Optional<Ingredient> ingredient = Optional.empty();
                if (k >= 0 && l >= 0 && k < this.getWidth() && l < this.getHeight()) {
                    ingredient = ingredientAt(k + l * this.getWidth());
                }
                if (accepts(ingredient, craftingInventory.getItem(i + j * craftingInventory.width()))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<StrictShapedRecipe> getSerializer() {
        return SERIALIZER;
    }
}
