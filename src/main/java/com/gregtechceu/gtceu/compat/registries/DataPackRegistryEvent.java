package com.gregtechceu.gtceu.compat.registries;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

public abstract class DataPackRegistryEvent extends Event implements IModBusEvent {

    /** Registers datapack (dynamic) registries through Fabric. */
    public static class NewRegistry extends DataPackRegistryEvent {

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec) {
            dataPackRegistry(key, codec, null);
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec,
                                         @Nullable Codec<T> networkCodec) {
            if (networkCodec != null) {
                DynamicRegistries.registerSynced(key, codec, networkCodec);
            } else {
                DynamicRegistries.register(key, codec);
            }
        }
    }
}
