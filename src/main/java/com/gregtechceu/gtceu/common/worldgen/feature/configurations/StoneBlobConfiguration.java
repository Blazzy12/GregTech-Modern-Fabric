package com.gregtechceu.gtceu.common.worldgen.feature.configurations;

import net.minecraft.util.valueproviders.IntProvider;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;

public record StoneBlobConfiguration(BlockReplacement state, IntProvider size) {

    public static final MapCodec<StoneBlobConfiguration> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BlockReplacement.CODEC.fieldOf("state").forGetter(StoneBlobConfiguration::state),
                    net.minecraft.util.valueproviders.IntProviders.codec(1, 64).fieldOf("size").forGetter(StoneBlobConfiguration::size))
                    .apply(instance, StoneBlobConfiguration::new));
}
