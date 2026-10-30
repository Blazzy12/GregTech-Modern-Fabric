package com.gregtechceu.gtceu.compat.network.registration;

import com.gregtechceu.gtceu.compat.network.handling.IPayloadContext;
import com.gregtechceu.gtceu.compat.network.handling.IPayloadHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;

/** Registers play payloads with Fabric networking. Handlers run on the main thread. */
public class PayloadRegistrar {

    private final String version;

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    public PayloadRegistrar versioned(String version) {
        return new PayloadRegistrar(version);
    }

    public PayloadRegistrar optional() {
        return this;
    }

    public PayloadRegistrar executesOn(Object thread) {
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPayloadBridge.register(type, handler);
        }
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload,
                new IPayloadContext() {

                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public PacketFlow flow() {
                        return PacketFlow.SERVERBOUND;
                    }

                    @Override
                    public void reply(CustomPacketPayload reply) {
                        context.responseSender().sendPacket(reply);
                    }

                    @Override
                    public CompletableFuture<Void> enqueueWork(Runnable task) {
                        return context.server().submit(task);
                    }

                    @Override
                    public void disconnect(Component reason) {
                        context.player().connection.disconnect(reason);
                    }
                }));
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        playToClient(type, codec, handler);
        playToServer(type, codec, handler);
        return this;
    }

    public String version() {
        return version;
    }
}
