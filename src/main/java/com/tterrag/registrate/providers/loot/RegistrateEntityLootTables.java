package com.tterrag.registrate.providers.loot;

import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;

/** Datagen-only entity loot. Datagen does not run on Fabric. */
public class RegistrateEntityLootTables extends EntityLootSubProvider implements RegistrateLootTables {

    public RegistrateEntityLootTables(net.minecraft.data.loot.LootTableSubProvider.Context context) {
        super(FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    public void generate() {}

    public void add(EntityType<?> type, LootTable.Builder table) {
        super.add(type, table);
    }
}
