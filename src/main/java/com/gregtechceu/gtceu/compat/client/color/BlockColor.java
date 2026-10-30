package com.gregtechceu.gtceu.compat.client.color;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/** Pre-26.x block color handler, bridged to tint sources by {@link ColorHandlers}. */
@FunctionalInterface
public interface BlockColor {

    int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
}
