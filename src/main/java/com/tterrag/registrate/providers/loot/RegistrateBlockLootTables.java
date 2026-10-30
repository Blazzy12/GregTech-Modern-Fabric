package com.tterrag.registrate.providers.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Set;

/** Block loot for Registrate builders. Datagen does not run on Fabric, so this only needs to compile. */
public class RegistrateBlockLootTables extends BlockLootSubProvider implements RegistrateLootTables {

    public RegistrateBlockLootTables(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    public void generate() {}

    public net.minecraft.core.HolderGetter<Block> blockLookup() {
        return this.blocks;
    }

    @Override
    public void add(Block block, LootTable.Builder builder) {
        super.add(block, builder);
    }

    @Override
    public void dropSelf(Block block) {
        super.dropSelf(block);
    }

    @Override
    public void dropOther(Block block, ItemLike drop) {
        super.dropOther(block, drop);
    }
}
