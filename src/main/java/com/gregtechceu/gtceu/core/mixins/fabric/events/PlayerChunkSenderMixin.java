package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.level.ChunkWatchEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Posts chunk watch events when a chunk is sent to a player or dropped. */
@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {

    @Inject(method = "sendChunk", at = @At("TAIL"))
    private static void gtceu$watch(ServerGamePacketListenerImpl connection, ServerLevel level, LevelChunk chunk,
                                    CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(connection.player, chunk.getPos(), level, chunk));
    }

    @Inject(method = "dropChunk", at = @At("HEAD"))
    private void gtceu$unwatch(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ChunkWatchEvent.UnWatch(player, pos, player.level()));
    }
}
