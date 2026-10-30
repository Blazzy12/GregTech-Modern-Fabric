package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

public class GTConfiguredFeatures {

    // spotless:off
    public static final ResourceKey<Feature> RUBBER_TREE = create("rubber_tree");
    public static final ResourceKey<Feature> RED_GRANITE_BLOB = create("red_granite_blob");
    public static final ResourceKey<Feature> MARBLE_BLOB = create("marble_blob");
    public static final ResourceKey<Feature> RAW_OIL_SPROUT = create("raw_oil_sprout");

    // spotless:on

    private static ResourceKey<Feature> create(String path) {
        return ResourceKey.create(Registries.FEATURE, GTCEu.id(path));
    }
}
