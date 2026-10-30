package com.gregtechceu.gtceu.compat.common;

import com.gregtechceu.gtceu.compat.server.ServerLifecycleHooks;

import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Remembers player names by UUID, filled as players join, with the server's profile cache as a fallback. */
public final class UsernameCache {

    private static final Map<UUID, String> CACHE = new ConcurrentHashMap<>();

    private UsernameCache() {}

    public static void setUsername(UUID uuid, String name) {
        CACHE.put(uuid, name);
    }

    public static @Nullable String getLastKnownUsername(UUID uuid) {
        String cached = CACHE.get(uuid);
        if (cached != null) return cached;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var player = server.getPlayerList().getPlayer(uuid);
            if (player != null) return player.getGameProfile().name();
        }
        return null;
    }

    public static boolean containsUUID(UUID uuid) {
        return CACHE.containsKey(uuid);
    }
}
