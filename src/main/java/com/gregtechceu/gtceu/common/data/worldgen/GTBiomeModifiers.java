package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.compat.common.Tags;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;

/** Adds GT's placed features to biomes through Fabric's biome modification API. */
public class GTBiomeModifiers {

    public static void init() {
        BiomeModifications.addFeature(BiomeSelectors.tag(CustomTags.HAS_RUBBER_TREE),
                GenerationStep.Decoration.VEGETAL_DECORATION, GTPlacedFeatures.RUBBER_TREE);
        BiomeModifications.addFeature(BiomeSelectors.tag(Tags.Biomes.IS_SWAMP),
                GenerationStep.Decoration.VEGETAL_DECORATION, GTPlacedFeatures.RUBBER_TREE_SWAMP);
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.UNDERGROUND_ORES, GTPlacedFeatures.RED_GRANITE_BLOB);
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.UNDERGROUND_ORES, GTPlacedFeatures.MARBLE_BLOB);
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.FLUID_SPRINGS, GTPlacedFeatures.RAW_OIL_SPROUT);
    }
}
