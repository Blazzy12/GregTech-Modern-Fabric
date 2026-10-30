package com.gregtechceu.gtceu.compat.network.handling;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;

public interface IPayloadContext {

    /** The sending player on the server, or the local player on the client. */
    Player player();

    /** {@link PacketFlow#SERVERBOUND} when received by the server. */
    PacketFlow flow();

    void reply(CustomPacketPayload payload);

    CompletableFuture<Void> enqueueWork(Runnable task);

    void disconnect(Component reason);
}
