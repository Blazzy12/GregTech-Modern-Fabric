package com.tterrag.registrate.providers.loot;

import com.gregtechceu.gtceu.compat.fml.LogicalSide;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/** Datagen-only loot provider; loot actions are recorded but never run on Fabric. */
public class RegistrateLootTableProvider implements RegistrateProvider {

    public interface LootType<T extends RegistrateLootTables> {

        LootType<RegistrateBlockLootTables> BLOCK = new LootType<>() {};
        LootType<RegistrateEntityLootTables> ENTITY = new LootType<>() {};
    }

    private final AbstractRegistrate<?> owner;

    public RegistrateLootTableProvider(AbstractRegistrate<?> parent, PackOutput packOutput,
                                       CompletableFuture<HolderLookup.Provider> provider) {
        this.owner = parent;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public <T extends RegistrateLootTables> void addLootAction(LootType<T> type, NonNullConsumer<T> action) {}

    @Override
    public String getName() {
        return "Loot tables (" + owner.getModid() + ")";
    }
}
