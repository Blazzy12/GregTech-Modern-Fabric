package com.gregtechceu.gtceu.common.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** The placement arguments of a feature, with the accessor names of the removed {@code FeaturePlaceContext}. */
public record GTFeaturePlaceContext<C>(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random,
                                       BlockPos origin, C config) {}
