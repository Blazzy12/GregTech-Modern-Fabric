package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.GTSavedData;

import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.*;

public class MultiblockWorldSavedData extends GTSavedData {

    public static MultiblockWorldSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(GTSavedData.type("gtceu_multiblock", MultiblockWorldSavedData::new, MultiblockWorldSavedData::new));
    }

    /**
     * Store all formed multiblocks' structure info
     */
    public final Map<BlockPos, Set<PatternState>> mapping;
    /**
     * Chunk pos mapping.
     */
    public final Map<ChunkPos, Set<PatternState>> chunkPosMapping;

    private MultiblockWorldSavedData() {
        this.mapping = new Object2ObjectOpenHashMap<>();
        this.chunkPosMapping = new HashMap<>();
    }

    private MultiblockWorldSavedData(CompoundTag tag, HolderLookup.Provider access) {
        this();
    }

    public PatternState[] getPatternsInChunk(ChunkPos chunkPos) {
        return chunkPosMapping.getOrDefault(chunkPos, Collections.emptySet()).toArray(PatternState[]::new);
    }

    public void addMapping(PatternState patternState) {
        this.mapping.computeIfAbsent(patternState.getControllerPos(), x -> new HashSet<>()).add(patternState);
        for (long pos : patternState.getCache().keySet()) {
            chunkPosMapping.computeIfAbsent(ChunkPos.containing(BlockPos.of(pos)), c -> new HashSet<>()).add(patternState);
        }
    }

    public void removeMapping(PatternState patternState) {
        this.mapping.getOrDefault(patternState.getControllerPos(), new HashSet<>()).remove(patternState);
        this.mapping.entrySet().removeIf(e -> e.getValue().isEmpty());

        for (var patternSet : chunkPosMapping.values()) {
            patternSet.remove(patternState);
        }
        chunkPosMapping.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider lookup) {
        return compound;
    }
}
