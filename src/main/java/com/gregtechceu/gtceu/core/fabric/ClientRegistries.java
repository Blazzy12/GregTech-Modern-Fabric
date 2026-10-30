package com.gregtechceu.gtceu.core.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

import org.jetbrains.annotations.Nullable;

final class ClientRegistries {

    private ClientRegistries() {}

    static @Nullable RegistryAccess access() {
        var connection = Minecraft.getInstance().getConnection();
        return connection != null ? connection.registryAccess() : null;
    }
}
