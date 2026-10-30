package com.gregtechceu.gtceu.client.renderer.cover;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;

import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.gregtechceu.gtceu.compat.client.model.ChunkRenderTypeSet;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoverRenderer {

    @Environment(EnvType.CLIENT)
    void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                     CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                     ModelData modelData, @Nullable ChunkSectionLayer renderType);

    default ChunkRenderTypeSet getRenderTypes(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                              RandomSource rand, ModelData modelData) {
        return ChunkRenderTypeSet.of(ChunkSectionLayer.CUTOUT);
    }

    default ModelData getModelData(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                   ModelData holderModelData) {
        return ModelData.EMPTY;
    }

    default boolean shouldRenderBackPlateForSide(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                                 @Nullable Direction side) {
        return true;
    }
}
