package com.gregtechceu.gtceu.compat.network.registration;

import com.gregtechceu.gtceu.compat.network.handling.IPayloadContext;
import com.gregtechceu.gtceu.compat.network.handling.IPayloadHandler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;

/** Client-only half of {@link PayloadRegistrar}; never loaded on a dedicated server. */
final class ClientPayloadBridge {

    private ClientPayloadBridge() {}

    static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type,
                                                         IPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload,
                new IPayloadContext() {

                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public PacketFlow flow() {
                        return PacketFlow.CLIENTBOUND;
                    }

                    @Override
                    public void reply(CustomPacketPayload reply) {
                        context.responseSender().sendPacket(reply);
                    }

                    @Override
                    public CompletableFuture<Void> enqueueWork(Runnable task) {
                        return context.client().submit(task);
                    }

                    @Override
                    public void disconnect(Component reason) {
                        context.client().getConnection().getConnection().disconnect(reason);
                    }
                }));
    }

    static void sendToServer(CustomPacketPayload payload) {
        // dropped while not connected or when the server lacks the channel; Fabric would throw
        if (net.minecraft.client.Minecraft.getInstance().getConnection() != null
                && ClientPlayNetworking.canSend(payload.type())) {
            ClientPlayNetworking.send(payload);
        }
    }
}
