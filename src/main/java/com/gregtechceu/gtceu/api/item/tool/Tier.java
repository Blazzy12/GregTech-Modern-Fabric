package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * A tool tier. 26.x replaced tiers with the {@link ToolMaterial} record; GT keeps this interface for its own
 * tool system and converts with {@link #toToolMaterial()} where vanilla needs one.
 */
public interface Tier {

    TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, GTCEu.id("no_repair"));

    int getUses();

    float getSpeed();

    float getAttackDamageBonus();

    TagKey<Block> getIncorrectBlocksForDrops();

    int getEnchantmentValue();

    /** @return the repair ingredient, or null when the tool cannot be repaired */
    @org.jetbrains.annotations.Nullable
    Ingredient getRepairIngredient();

    default ToolMaterial toToolMaterial() {
        return new ToolMaterial(getIncorrectBlocksForDrops(), getUses(), getSpeed(), getAttackDamageBonus(),
                getEnchantmentValue(), NO_REPAIR);
    }
}
