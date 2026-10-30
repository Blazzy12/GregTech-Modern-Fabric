package com.gregtechceu.gtceu.compat.registries;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import org.jetbrains.annotations.Nullable;

/** Builds a static registry through Fabric, registering it with the root registry immediately. */
public class RegistryBuilder<T> {

    private final ResourceKey<? extends Registry<T>> key;
    private boolean sync;
    private @Nullable Identifier defaultKey;

    public RegistryBuilder(ResourceKey<? extends Registry<T>> key) {
        this.key = key;
    }

    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public RegistryBuilder<T> defaultKey(Identifier defaultKey) {
        this.defaultKey = defaultKey;
        return this;
    }

    public RegistryBuilder<T> defaultKey(ResourceKey<T> defaultKey) {
        return defaultKey(defaultKey.identifier());
    }

    public RegistryBuilder<T> maxId(int maxId) {
        return this;
    }

    @SuppressWarnings("unchecked")
    public Registry<T> create() {
        ResourceKey<Registry<T>> registryKey = (ResourceKey<Registry<T>>) key;
        if (defaultKey != null) {
            var builder = FabricRegistryBuilder.createDefaulted(registryKey, defaultKey);
            if (sync) builder.attribute(RegistryAttribute.SYNCED);
            return builder.buildAndRegister();
        }
        var builder = FabricRegistryBuilder.create(registryKey);
        if (sync) builder.attribute(RegistryAttribute.SYNCED);
        return builder.buildAndRegister();
    }
}
