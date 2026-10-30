package com.gregtechceu.gtceu.common.worldgen.modifier;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import com.mojang.serialization.MapCodec;

import java.util.List;
import java.util.function.Consumer;

public class BiomeDependentPlacement implements PlacementModifier {

    public static final MapCodec<BiomeDependentPlacement> CODEC = BiomeWeightModifier.CODEC.listOf()
            .fieldOf("modifiers").xmap(BiomeDependentPlacement::new, placement -> placement.modifiers);

    public final List<BiomeWeightModifier> modifiers;

    public BiomeDependentPlacement(List<BiomeWeightModifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos pos, Consumer<BlockPos> output) {
        for (BiomeWeightModifier modifier : modifiers) {
            if (modifier.biomes.contains(context.getLevel().getBiome(pos))) {
                if (modifier.addedWeight < 100 && random.nextInt(100) >= modifier.addedWeight) {
                    return;
                }
            }
        }
        output.accept(pos);
    }

    @Override
    public MapCodec<? extends PlacementModifier> codec() {
        return CODEC;
    }
}
