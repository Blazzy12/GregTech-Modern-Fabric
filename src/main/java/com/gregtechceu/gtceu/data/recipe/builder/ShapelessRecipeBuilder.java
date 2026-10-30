package com.gregtechceu.gtceu.data.recipe.builder;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import com.gregtechceu.gtceu.compat.common.crafting.DataComponentIngredient;

import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public class ShapelessRecipeBuilder {

    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    @Setter
    protected String group = "";
    @Setter
    private CraftingBookCategory category = CraftingBookCategory.MISC;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    protected Identifier id;

    public ShapelessRecipeBuilder(Identifier id) {
        this.id = id;
    }

    public ShapelessRecipeBuilder requires(TagKey<Item> itemStack) {
        return requires(Ingredient.of(com.gregtechceu.gtceu.utils.TagUtil.holderSet(net.minecraft.core.registries.BuiltInRegistries.ITEM, itemStack)));
    }

    public ShapelessRecipeBuilder requires(ItemStack itemStack) {
        if (!itemStack.getComponentsPatch().isEmpty()) {
            requires(DataComponentIngredient.of(true, itemStack));
        } else {
            requires(Ingredient.of((itemStack).getItem()));
        }
        return this;
    }

    public ShapelessRecipeBuilder requires(ItemLike itemLike) {
        return requires(Ingredient.of(itemLike));
    }

    public ShapelessRecipeBuilder requires(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public ShapelessRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public ShapelessRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    public ShapelessRecipe build() {
        return new ShapelessRecipe(new net.minecraft.world.item.crafting.Recipe.CommonInfo(false),
                new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(this.category,
                        java.util.Objects.requireNonNullElse(this.group, "")),
                net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(this.output), this.ingredients);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id;

        consumer.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, recipeId.withPrefix("shapeless/")), build(), null);
    }
}
