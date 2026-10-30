package com.gregtechceu.gtceu.common.data.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.condition.GTConfigValueCondition;

import net.minecraft.core.registries.Registries;
import com.gregtechceu.gtceu.compat.bus.IEventBus;
import com.gregtechceu.gtceu.compat.registries.DeferredHolder;
import com.gregtechceu.gtceu.compat.registries.DeferredRegister;

public class GTLootConditions {
    // spotless:off

    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GTCEu.MOD_ID);


    // 26.x loot condition types are just their codecs
    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<GTConfigValueCondition>> CONFIG_VALUE = LOOT_CONDITION_TYPES.register("config_value", () -> GTConfigValueCondition.CODEC);

    // spotless:on

    public static void init(IEventBus modBus) {
        LOOT_CONDITION_TYPES.register(modBus);
    }
}
