package com.gregtechceu.gtceu.compat.network;

import com.gregtechceu.gtceu.compat.server.ServerLifecycleHooks;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class PacketDistributor {

    private PacketDistributor() {}

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientSender.send(payload);
        for (CustomPacketPayload p : payloads) ClientSender.send(p);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload,
                                    CustomPacketPayload... payloads) {
        send(player, payload, payloads);
    }

    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload,
                                                CustomPacketPayload... payloads) {
        sendAll(PlayerLookup.level(level), payload, payloads);
    }

    public static void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y,
                                         double z, double radius, CustomPacketPayload payload,
                                         CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.around(level, new Vec3(x, y, z), radius)) {
            if (player != excluded) send(player, payload, payloads);
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) sendAll(PlayerLookup.all(server), payload, payloads);
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload,
                                                   CustomPacketPayload... payloads) {
        sendAll(PlayerLookup.tracking(entity), payload, payloads);
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload,
                                                          CustomPacketPayload... payloads) {
        sendToPlayersTrackingEntity(entity, payload, payloads);
        if (entity instanceof ServerPlayer player) send(player, payload, payloads);
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload,
                                                  CustomPacketPayload... payloads) {
        sendAll(PlayerLookup.tracking(level, chunkPos), payload, payloads);
    }

    public static void sendToPlayersTrackingBlock(ServerLevel level, BlockPos pos, CustomPacketPayload payload,
                                                  CustomPacketPayload... payloads) {
        sendAll(PlayerLookup.tracking(level, pos), payload, payloads);
    }

    private static void sendAll(Collection<ServerPlayer> players, CustomPacketPayload payload,
                                CustomPacketPayload... payloads) {
        for (ServerPlayer player : players) send(player, payload, payloads);
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ServerPlayNetworking.send(player, payload);
        for (CustomPacketPayload p : payloads) ServerPlayNetworking.send(player, p);
    }

    private static final class ClientSender {

        /**
         * Like NeoForge, packets sent while not connected (or to a server without the channel) are dropped instead of
         * crashing: Fabric throws in both cases.
         */
        static void send(CustomPacketPayload payload) {
            if (net.minecraft.client.Minecraft.getInstance().getConnection() == null
                    || !net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(payload.type())) {
                return;
            }
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
        }
    }
}
