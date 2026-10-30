package com.gregtechceu.gtceu.compat.event.level;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public abstract class ChunkWatchEvent extends Event {

    private final ServerPlayer player;
    private final ChunkPos pos;
    private final ServerLevel level;

    public ChunkWatchEvent(ServerPlayer player, ChunkPos pos, ServerLevel level) {
        this.player = player;
        this.pos = pos;
        this.level = level;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ChunkPos getPos() {
        return pos;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public static class Watch extends ChunkWatchEvent {

        private final LevelChunk chunk;

        public Watch(ServerPlayer player, ChunkPos pos, ServerLevel level, LevelChunk chunk) {
            super(player, pos, level);
            this.chunk = chunk;
        }

        public LevelChunk getChunk() {
            return chunk;
        }
    }

    public static class UnWatch extends ChunkWatchEvent {

        public UnWatch(ServerPlayer player, ChunkPos pos, ServerLevel level) {
            super(player, pos, level);
        }
    }
}
