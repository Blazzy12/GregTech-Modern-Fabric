package com.gregtechceu.gtceu.compat.client.extensions.common;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/** Client-side block hooks for custom break and hit particles. */
public interface IClientBlockExtensions {

    IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {};

    static IClientBlockExtensions of(Block block) {
        return ClientExtensionsManager.BLOCK_EXTENSIONS.getOrDefault(block, DEFAULT);
    }

    static IClientBlockExtensions of(BlockState state) {
        return of(state.getBlock());
    }

    /** @return true to suppress the vanilla hit particles */
    default boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        return false;
    }

    /** @return true to suppress the vanilla break particles */
    default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return false;
    }
}
