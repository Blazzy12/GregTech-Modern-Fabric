package com.gregtechceu.gtceu.compat.registries.datamaps.builtin;

import com.gregtechceu.gtceu.compat.registries.datamaps.DataMapType;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public final class NeoForgeDataMaps {

    public static final DataMapType<Item, Compostable> COMPOSTABLES = new DataMapType<>(Registries.ITEM,
            Identifier.fromNamespaceAndPath("neoforge", "compostables"),
            (item, value) -> modify(item, builder -> builder.set(DataComponents.COMPOSTABLE,
                    new net.minecraft.world.item.component.Compostable(compostTier(value.chance())))));

    public static final DataMapType<Item, FurnaceFuel> FURNACE_FUELS = new DataMapType<>(Registries.ITEM,
            Identifier.fromNamespaceAndPath("neoforge", "furnace_fuels"),
            (item, value) -> modify(item, builder -> builder.set(DataComponents.COOKING_FUEL,
                    new CookingFuel(new ResolvableInt.Constant(value.burnTime()),
                            new ResolvableFloat.Constant(1.0F)))));

    private NeoForgeDataMaps() {}

    /** Makes {@code item} a furnace fuel burning for {@code burnTime} ticks (NeoForge's {@code getBurnTime}). */
    public static void setBurnTime(Item item, int burnTime) {
        if (burnTime <= 0) return;
        modify(item, builder -> builder.set(DataComponents.COOKING_FUEL,
                new CookingFuel(new ResolvableInt.Constant(burnTime), new ResolvableFloat.Constant(1.0F))));
    }

    private static void modify(Item item,
                               java.util.function.Consumer<net.minecraft.core.component.DataComponentMap.Builder> action) {
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(item, action));
    }

    /** The vanilla tiers for the 1.21.1 chances 0.3, 0.5, 0.65, 0.85 and 1.0. */
    private static ResourceKey<ContextIntProvider> compostTier(float chance) {
        if (chance >= 1.0F) return ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE;
        if (chance >= 0.85F) return ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH;
        if (chance >= 0.65F) return ContextIntProviders.COMPOSTABLE_MEDIUM;
        if (chance >= 0.5F) return ContextIntProviders.COMPOSTABLE_LOW_MEDIUM;
        return ContextIntProviders.COMPOSTABLE_LOW;
    }
}
