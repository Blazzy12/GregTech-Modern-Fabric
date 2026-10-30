package com.gregtechceu.gtceu.compat.common.extensions;

import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

/**
 * NeoForge's block entity hooks. {@link #onLoad} and {@link #onChunkUnloaded} are called from Fabric's block entity
 * load and unload events.
 */
public interface IBlockEntityExtension {

    default void onLoad() {}

    default void onChunkUnloaded() {}

    default ModelData getModelData() {
        return ModelData.EMPTY;
    }

    /** Asks the client to rebuild the section containing this block entity. */
    default void requestModelDataUpdate() {
        if (this instanceof net.minecraft.world.level.block.entity.BlockEntity be && be.getLevel() != null &&
                be.getLevel().isClientSide()) {
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 8);
        }
    }
}
