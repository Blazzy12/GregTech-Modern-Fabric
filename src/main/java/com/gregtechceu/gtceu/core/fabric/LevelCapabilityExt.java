package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.capabilities.BlockCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/** Injected into {@link Level}: NeoForge-style capability queries backed by Fabric API Lookup. */
public interface LevelCapabilityExt {

    default <T, C> @Nullable T getCapability(BlockCapability<T, C> capability, BlockPos pos, C context) {
        return capability.getCapability((Level) this, pos, null, null, context);
    }

    default <T, C> @Nullable T getCapability(BlockCapability<T, C> capability, BlockPos pos,
                                             @Nullable BlockState state, @Nullable BlockEntity blockEntity,
                                             C context) {
        return capability.getCapability((Level) this, pos, state, blockEntity, context);
    }

    /** Fabric lookups are not cached per level, so there is nothing to invalidate. */
    default void invalidateCapabilities(BlockPos pos) {}
}
