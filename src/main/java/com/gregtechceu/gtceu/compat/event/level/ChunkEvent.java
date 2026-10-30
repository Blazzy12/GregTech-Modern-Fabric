package com.gregtechceu.gtceu.compat.event.level;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class ChunkEvent extends Event {

    private final ChunkAccess chunk;
    private final LevelAccessor level;

    public ChunkEvent(ChunkAccess chunk, LevelAccessor level) {
        this.chunk = chunk;
        this.level = level;
    }

    public ChunkAccess getChunk() {
        return chunk;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public static class Load extends ChunkEvent {

        private final boolean newChunk;

        public Load(ChunkAccess chunk, LevelAccessor level, boolean newChunk) {
            super(chunk, level);
            this.newChunk = newChunk;
        }

        public boolean getNewChunk() {
            return newChunk;
        }
    }

    public static class Unload extends ChunkEvent {

        public Unload(ChunkAccess chunk, LevelAccessor level) {
            super(chunk, level);
        }
    }
}
