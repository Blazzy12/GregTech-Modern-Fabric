package com.gregtechceu.gtceu.compat.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

public final class ServerLifecycleHooks {

    private static @Nullable MinecraftServer currentServer;

    private ServerLifecycleHooks() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
    }

    public static @Nullable MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
