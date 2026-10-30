package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.data.pack.KeyOnlyLookups;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.stream.Stream;

/**
 * Lets vanilla's loot sub-provider helpers ({@code BlockLootSubProvider#createSilkTouchDispatchTable} and friends)
 * run outside datagen, so GT can build its material loot tables at runtime. Tables are built with
 * {@code LootTable.Builder#build()} by the caller, so {@link #accept} is never used.
 */
public record RuntimeLootContext(HolderLookup.Provider registries) implements LootTableSubProvider.Context {

    public static RuntimeLootContext of(HolderLookup.Provider registries) {
        return new RuntimeLootContext(registries);
    }

    @Override
    public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
        throw new UnsupportedOperationException("Runtime loot tables are built by the caller");
    }

    @Override
    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        return KeyOnlyLookups.with(registries).lookupOrThrow(key);
    }

    @Override
    @Deprecated
    public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return registries.lookupOrThrow(key).listElements();
    }
}
