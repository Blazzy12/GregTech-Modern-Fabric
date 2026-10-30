package com.gregtechceu.gtceu.client.model.machine;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IControllerModelRenderer {

    /**
     * Render a specific model for given part.
     */
    @Environment(EnvType.CLIENT)
    void renderPartModel(List<BakedQuad> quads, MultiblockControllerMachine machine, MultiblockPartMachine part,
                         Direction frontFacing, @Nullable Direction side, RandomSource rand,
                         @NotNull ModelData modelData, @Nullable ChunkSectionLayer renderType);
}
