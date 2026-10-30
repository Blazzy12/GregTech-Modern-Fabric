package com.gregtechceu.gtceu.compat.registries;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Posted once per registry, in registration order, so entries can be added to it. */
public class RegisterEvent extends Event implements IModBusEvent {

    private final ResourceKey<? extends Registry<?>> registryKey;
    private final Registry<?> registry;

    public RegisterEvent(ResourceKey<? extends Registry<?>> registryKey, Registry<?> registry) {
        this.registryKey = registryKey;
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, Identifier name,
                             Supplier<T> valueSupplier) {
        if (this.registryKey.equals(registryKey)) {
            Registry.register((Registry<T>) registry, name, valueSupplier.get());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, Consumer<RegisterHelper<T>> consumer) {
        if (this.registryKey.equals(registryKey)) {
            consumer.accept((name, value) -> Registry.register((Registry<T>) registry, name, value));
        }
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return registryKey;
    }

    public Registry<?> getRegistry() {
        return registry;
    }

    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return key.equals(registryKey) ? (Registry<T>) registry : null;
    }

    @FunctionalInterface
    public interface RegisterHelper<T> {

        void register(Identifier name, T value);

        default void register(ResourceKey<T> key, T value) {
            register(key.identifier(), value);
        }
    }
}
