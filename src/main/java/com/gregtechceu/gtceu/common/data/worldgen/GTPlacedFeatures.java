package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.*;

public class GTPlacedFeatures {

    public static final ResourceKey<PlacedFeature> RUBBER_TREE = create("rubber_tree");
    public static final ResourceKey<PlacedFeature> RUBBER_TREE_SWAMP = create("rubber_tree_swamp");
    public static final ResourceKey<PlacedFeature> RED_GRANITE_BLOB = create("red_granite_blob");
    public static final ResourceKey<PlacedFeature> MARBLE_BLOB = create("marble_blob");
    public static final ResourceKey<PlacedFeature> RAW_OIL_SPROUT = create("raw_oil_sprout");

    private static ResourceKey<PlacedFeature> create(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, GTCEu.id(path));
    }
}
