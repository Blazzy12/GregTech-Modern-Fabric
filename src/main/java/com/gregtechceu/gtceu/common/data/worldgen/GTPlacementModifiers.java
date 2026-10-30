package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.modifier.BiomeDependentPlacement;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GTPlacementModifiers {

    public static void init() {
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.id("biome_dependent"),
                BiomeDependentPlacement.CODEC);
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.id("rubber_tree_chance"),
                RubberTreeChancePlacement.CODEC);
    }
}
