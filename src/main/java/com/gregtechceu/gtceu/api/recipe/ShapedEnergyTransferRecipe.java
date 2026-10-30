package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

public class ShapedEnergyTransferRecipe extends GTShapedRecipe {

    public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(GTShapedRecipe::commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(GTShapedRecipe::bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(GTShapedRecipe::pattern),
            Ingredient.CODEC.fieldOf("chargeIngredient").forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
            Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
            Codec.BOOL.fieldOf("transferMaxCharge").forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(GTShapedRecipe::result))
            .apply(i, ShapedEnergyTransferRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    Recipe.CommonInfo.STREAM_CODEC, GTShapedRecipe::commonInfo,
                    CraftingRecipe.CraftingBookInfo.STREAM_CODEC, GTShapedRecipe::bookInfo,
                    ShapedRecipePattern.STREAM_CODEC, GTShapedRecipe::pattern,
                    Ingredient.CONTENTS_STREAM_CODEC, ShapedEnergyTransferRecipe::getChargeIngredient,
                    ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isOverrideCharge,
                    ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isTransferMaxCharge,
                    ItemStackTemplate.STREAM_CODEC, GTShapedRecipe::result,
                    ShapedEnergyTransferRecipe::new);

    public static final RecipeSerializer<ShapedEnergyTransferRecipe> SERIALIZER = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);

    @Getter
    private final Ingredient chargeIngredient;
    @Getter
    private final boolean transferMaxCharge;
    @Getter
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, Ingredient chargeIngredient,
                                      boolean overrideCharge, boolean transferMaxCharge, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
            for (int i = 0; i < craftingContainer.size(); i++) {
                if (ItemStack.isSameItem(craftingContainer.getItem(i), chargeStack)) {
                    ItemStack stack = craftingContainer.getItem(i);
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                        resultStack.set(GTDataComponents.ENERGY_CONTENT, new SimpleEnergyContent(maxCharge, charge));
                        return resultStack;
                    }
                }
            }
        }
        return resultStack;
    }

    @Override
    public RecipeSerializer<ShapedEnergyTransferRecipe> getSerializer() {
        return SERIALIZER;
    }
}
