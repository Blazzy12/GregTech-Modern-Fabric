package com.gregtechceu.gtceu.common.data.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.function.SetEUChargeFunction;

import net.minecraft.core.registries.Registries;
import com.gregtechceu.gtceu.compat.bus.IEventBus;
import com.gregtechceu.gtceu.compat.registries.DeferredHolder;
import com.gregtechceu.gtceu.compat.registries.DeferredRegister;

public class GTLootFunctions {
    // spotless:off

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, GTCEu.MOD_ID);


    // 26.x loot function types are just their codecs
    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<SetEUChargeFunction>> SET_EU_CHARGE = LOOT_FUNCTION_TYPES.register("set_eu_charge", () -> SetEUChargeFunction.CODEC);

    // spotless:on

    public static void init(IEventBus modBus) {
        LOOT_FUNCTION_TYPES.register(modBus);
    }
}
