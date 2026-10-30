package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.server.ServerLifecycleHooks;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;

/** The best available registry access when no level is at hand. */
public final class Registries {

    private Registries() {}

    public static RegistryAccess access() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            RegistryAccess client = ClientRegistries.access();
            if (client != null) return client;
        }
        throw new IllegalStateException("No registry access available");
    }
}
