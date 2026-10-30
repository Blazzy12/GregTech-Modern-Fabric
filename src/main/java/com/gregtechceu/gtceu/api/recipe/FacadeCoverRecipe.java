package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.MapCodec;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public class FacadeCoverRecipe extends CustomRecipe {

    public static final Identifier ID = GTCEu.id("crafting/facade_cover");
    private static TagKey<Item> IRON_PLATE_TAG;

    public static final FacadeCoverRecipe INSTANCE = new FacadeCoverRecipe();
    public static final RecipeSerializer<FacadeCoverRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE),
            StreamCodec.unit(INSTANCE));

    public FacadeCoverRecipe() {}

    // the instance is made before tag prefixes are registered, so look the tag up on first use
    private static TagKey<Item> ironPlateTag() {
        if (IRON_PLATE_TAG == null) {
            IRON_PLATE_TAG = ChemicalHelper.getTag(TagPrefix.plate, GTMaterials.Iron);
        }
        return IRON_PLATE_TAG;
    }

    @Override
    public boolean matches(CraftingInput container, @NotNull Level level) {
        boolean foundPlate = false;
        boolean foundBlockItem = false;
        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                if (foundBlockItem) {
                    return false;
                }
                foundBlockItem = true;
            } else if (item.is(ironPlateTag())) {
                if (foundPlate) {
                    return false;
                }
                foundPlate = true;
            } else {
                return false;
            }
        }
        return foundBlockItem && foundPlate;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput container) {
        ItemStack itemStack = GTItems.COVER_FACADE.asStack();
        BlockState facadeState = null;

        for (int i = 0; i < container.size(); i++) {
            var item = container.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                facadeState = FacadeItemBehaviour.getFacadeState(item);
                break;
            }
        }
        if (facadeState != null) {
            FacadeItemBehaviour.setFacadeState(itemStack, facadeState);
            itemStack.setCount(6);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(com.gregtechceu.gtceu.core.fabric.Ingredients.EMPTY,
                Ingredient.of(com.gregtechceu.gtceu.utils.TagUtil.holderSet(net.minecraft.core.registries.BuiltInRegistries.ITEM, ironPlateTag())),
                Ingredient.of(com.gregtechceu.gtceu.utils.TagUtil.holderSet(net.minecraft.core.registries.BuiltInRegistries.ITEM, ironPlateTag())),
                Ingredient.of(com.gregtechceu.gtceu.utils.TagUtil.holderSet(net.minecraft.core.registries.BuiltInRegistries.ITEM, ironPlateTag())),
                Ingredient.of(Items.STONE));
    }



    @Override
    public @NotNull RecipeSerializer<FacadeCoverRecipe> getSerializer() {
        return SERIALIZER;
    }
}
