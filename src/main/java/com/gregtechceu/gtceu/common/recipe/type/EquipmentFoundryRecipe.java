package com.gregtechceu.gtceu.common.recipe.type;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.gregtechceu.gtceu.compat.items.wrapper.RecipeWrapper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFoundryRecipe implements Recipe<RecipeWrapper> {

    // spotless:off
    public static final MapCodec<EquipmentFoundryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("equipment").forGetter(EquipmentFoundryRecipe::getEquipment),
            TierEntry.CODEC.listOf(0, GTValues.TIER_COUNT).fieldOf("entries").forGetter(EquipmentFoundryRecipe::getEntries)
    ).apply(instance, EquipmentFoundryRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentFoundryRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, EquipmentFoundryRecipe::getEquipment,
            TierEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), EquipmentFoundryRecipe::getEntries,
            EquipmentFoundryRecipe::new
    );

    public static final RecipeSerializer<EquipmentFoundryRecipe> SERIALIZER = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);
    //spotless:on

    @Getter
    private final Ingredient equipment;
    @Getter
    private final List<TierEntry> entries;

    public EquipmentFoundryRecipe(Ingredient equipment, List<TierEntry> entries) {
        if (entries.isEmpty())
            throw new IllegalArgumentException("Equipment foundry recipe must have at least one entry");

        this.entries = entries;
        this.equipment = equipment;
    }

    public EquipmentFoundryRecipe(Ingredient equipment, @Nullable Ingredient[] moduleIngredients,
                                  @Nullable Holder<ItemModule>[] modules) {
        if (moduleIngredients.length != modules.length)
            throw new IllegalArgumentException("Ingredient and module array length must match");

        List<TierEntry> entries = new ArrayList<>();
        for (int i = 0; i < moduleIngredients.length; i++) {
            var ingredient = moduleIngredients[i];
            var module = modules[i];
            if (ingredient == null || module == null) continue;
            entries.add(new TierEntry(ingredient, module));
        }

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Equipment foundry recipe must have at least one entry");
        }

        this.equipment = equipment;
        this.entries = entries;
    }

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
        return matches(container.getItem(0), container.getItem(1), 0);
    }

    public boolean matches(ItemStack equipmentItem, ItemStack itemToApply, int slot) {
        if (!equipment.test(equipmentItem)) return false;

        for (var entry : entries) {
            Ingredient ingredient = entry.ingredient();
            ItemModule moduleToApply = entry.moduleForTier().value();
            if (ingredient.test(itemToApply)) {
                IModularItem modularItem = GTCapabilityHelper.getModularItem(equipmentItem);
                if (modularItem == null) continue;
                if (modularItem.attach(moduleToApply, itemToApply, slot, true) != null) return true;

            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper container) {
        ItemStack applyTo = container.getItem(0);
        ItemStack ingredient = container.getItem(1);
        applyToItem(applyTo, ingredient, 0);
        return applyTo;
    }

    public void applyToItem(ItemStack equipmentItem, ItemStack itemToApply, int slot) {
        if (!equipment.test(equipmentItem)) return;
        for (var entry : entries) {
            Ingredient ingredient = entry.ingredient();
            ItemModule moduleToApply = entry.moduleForTier().value();
            if (ingredient.test(itemToApply)) {
                IModularItem modularItem = GTCapabilityHelper.getModularItem(equipmentItem);
                if (modularItem == null) continue;
                if (modularItem.attach(moduleToApply, itemToApply, slot, false) != null) return;

            }
        }
    }


    public ItemStack getResultItem() {
        return GTItems.QUANTUM_CHESTPLATE.asStack();
    }

    @Override
    public RecipeSerializer<EquipmentFoundryRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<EquipmentFoundryRecipe> getType() {
        return (RecipeType<EquipmentFoundryRecipe>) GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return com.gregtechceu.gtceu.common.data.GTRecipeBookCategories.MACHINE;
    }

    public record TierEntry(Ingredient ingredient, Holder<ItemModule> moduleForTier) {

        // spotless:off
        public static final Codec<TierEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(TierEntry::ingredient),
                RegistryFixedCodec.create(GTRegistries.Keys.ITEM_MODULE).fieldOf("module").forGetter(TierEntry::moduleForTier)
        ).apply(instance, TierEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TierEntry> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, TierEntry::ingredient,
                ByteBufCodecs.holderRegistry(GTRegistries.Keys.ITEM_MODULE), TierEntry::moduleForTier,
                TierEntry::new
        );
        //spotless:on
    }

}
