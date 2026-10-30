package com.gregtechceu.gtceu.core.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's {@code IBlockExtension#onCaughtFire} for blocks that react to being set alight, as TNT does. Called by
 * fire spreading ({@code FireBlockMixin}) and by GT's lighters.
 */
public interface IIgnitableBlock {

    void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
                      @Nullable LivingEntity igniter);
}
