package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Base for GT's shaped crafting variants. Re-implements vanilla shaped matching on {@link ShapedRecipePattern}
 * so each subclass can have its own serializer type.
 */
public abstract class GTShapedRecipe extends NormalCraftingRecipe {

    protected final ShapedRecipePattern pattern;
    protected final ItemStackTemplate result;

    protected GTShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                             ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo);
        this.pattern = pattern;
        this.result = result;
    }

    public Recipe.CommonInfo commonInfo() {
        return commonInfo;
    }

    public CraftingRecipe.CraftingBookInfo bookInfo() {
        return bookInfo;
    }

    public ShapedRecipePattern pattern() {
        return pattern;
    }

    public ItemStackTemplate result() {
        return result;
    }

    public List<Optional<Ingredient>> getIngredients() {
        return pattern.ingredients();
    }

    public int getWidth() {
        return pattern.width();
    }

    public int getHeight() {
        return pattern.height();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return pattern.matches(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(pattern.ingredients());
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(pattern.width(), pattern.height(),
                pattern.ingredients().stream().map(e -> e.map(Ingredient::display)
                        .orElse(SlotDisplay.Empty.INSTANCE)).toList(),
                new SlotDisplay.ItemStackSlotDisplay(result), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    /** The ingredient at a pattern index; empty when that cell of the pattern is blank. */
    protected Optional<Ingredient> ingredientAt(int index) {
        return pattern.ingredients().get(index);
    }

    /** Whether a pattern cell accepts a stack. Blank cells only accept empty stacks. */
    protected static boolean accepts(Optional<Ingredient> ingredient, ItemStack stack) {
        return ingredient.map(i -> i.test(stack)).orElse(stack.isEmpty());
    }

    @FunctionalInterface
    public interface Factory<T extends GTShapedRecipe> {

        T create(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                 ShapedRecipePattern pattern, ItemStackTemplate result);
    }

    public static <T extends GTShapedRecipe> MapCodec<T> codec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(GTShapedRecipe::commonInfo),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(GTShapedRecipe::bookInfo),
                ShapedRecipePattern.MAP_CODEC.forGetter(GTShapedRecipe::pattern),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(GTShapedRecipe::result))
                .apply(i, factory::create));
    }

    public static <T extends GTShapedRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                Recipe.CommonInfo.STREAM_CODEC, GTShapedRecipe::commonInfo,
                CraftingRecipe.CraftingBookInfo.STREAM_CODEC, GTShapedRecipe::bookInfo,
                ShapedRecipePattern.STREAM_CODEC, GTShapedRecipe::pattern,
                ItemStackTemplate.STREAM_CODEC, GTShapedRecipe::result,
                factory::create);
    }
}
