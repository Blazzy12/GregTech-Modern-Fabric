package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ShapedFluidContainerRecipe extends GTShapedRecipe {

    public static final RecipeSerializer<ShapedFluidContainerRecipe> SERIALIZER = new RecipeSerializer<>(
            codec(ShapedFluidContainerRecipe::new), streamCodec(ShapedFluidContainerRecipe::new));

    public ShapedFluidContainerRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> items = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        // figure out all the fluid container ingredients' remainders.
        int replacedSlot = -1;
        OUTER_LOOP:
        for (int x = 0; x <= inv.width() - this.getWidth(); ++x) {
            for (int y = 0; y <= inv.height() - this.getHeight(); ++y) {
                var stack = this.findFluidReplacement(inv, x, y, false);
                if (stack != null) {
                    replacedSlot = stack.firstInt();
                    items.set(replacedSlot, stack.second());
                    break OUTER_LOOP;
                }

                stack = this.findFluidReplacement(inv, x, y, true);
                if (stack != null) {
                    replacedSlot = stack.firstInt();
                    items.set(replacedSlot, stack.second());
                    break OUTER_LOOP;
                }
            }
        }

        for (int i = 0; i < items.size(); ++i) {
            if (i == replacedSlot) {
                continue;
            }
            ItemStack item = inv.getItem(i);
            if (item.hasCraftingRemainingItem()) {
                items.set(i, item.getCraftingRemainingItem());
            }
        }

        return items;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    @Nullable
    private IntObjectPair<ItemStack> findFluidReplacement(CraftingInput inv, int width, int height, boolean mirrored) {
        for (int x = 0; x < inv.width(); ++x) {
            for (int y = 0; y < inv.height(); ++y) {
                int offsetX = x - width;
                int offsetY = y - height;
                Optional<Ingredient> ingredient = Optional.empty();
                if (offsetX >= 0 && offsetY >= 0 && offsetX < this.getWidth() && offsetY < this.getHeight()) {
                    if (mirrored) {
                        ingredient = ingredientAt(this.getWidth() - offsetX - 1 + offsetY * this.getWidth());
                    } else {
                        ingredient = ingredientAt(offsetX + offsetY * this.getWidth());
                    }
                }

                if (ingredient.isPresent() &&
                        ingredient.get().getCustomIngredient() instanceof FluidContainerIngredient fluidContainerIngredient) {
                    int slot = x + y * inv.width();
                    ItemStack stack = inv.getItem(slot);
                    if (fluidContainerIngredient.test(stack)) {
                        return IntObjectPair.of(slot, fluidContainerIngredient.getExtractedStack(stack));
                    }
                }
            }
        }

        return null;
    }

    @Override
    public RecipeSerializer<ShapedFluidContainerRecipe> getSerializer() {
        return SERIALIZER;
    }
}
