package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GTFeatures {

    public static void init() {
        Registry.register(BuiltInRegistries.FEATURE_TYPE, GTCEu.id("stone_blob"), StoneBlobFeature.CODEC);
        Registry.register(BuiltInRegistries.FEATURE_TYPE, GTCEu.id("fluid_sprout"), FluidSproutFeature.CODEC);
    }
}
