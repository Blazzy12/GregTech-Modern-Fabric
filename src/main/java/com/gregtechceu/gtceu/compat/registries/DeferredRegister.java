package com.gregtechceu.gtceu.compat.registries;

import com.gregtechceu.gtceu.compat.bus.IEventBus;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Collects registry entries and registers them when {@link #register(IEventBus)} is called. Fabric registries
 * are open during mod initialisation, so entries added afterwards are registered immediately.
 */
public class DeferredRegister<T> {

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private boolean registered;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    public static <T> DeferredRegister<T> create(Identifier registryName, String namespace) {
        return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), namespace);
    }

    public static DeferredRegister<DataComponentType<?>> createDataComponents(String namespace) {
        return create(Registries.DATA_COMPONENT_TYPE, namespace);
    }

    public static DeferredRegister<DataComponentType<?>> createDataComponents(
            ResourceKey<Registry<DataComponentType<?>>> key, String namespace) {
        return create(key, namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        return register(name, key -> sup.get());
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> func) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
        DeferredHolder<T, I> holder = createHolder(registryKey, id);
        Supplier<I> supplier = () -> func.apply(id);
        if (entries.putIfAbsent(holder, supplier) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }
        if (registered) registerEntry(holder, supplier);
        return holder;
    }

    public <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> registerComponentType(
            String name, UnaryOperator<DataComponentType.Builder<D>> builder) {
        @SuppressWarnings("unchecked")
        DeferredRegister<DataComponentType<?>> self = (DeferredRegister<DataComponentType<?>>) (Object) this;
        return self.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    @SuppressWarnings("unchecked")
    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey,
                                                              Identifier key) {
        return (DeferredHolder<T, I>) DeferredHolder.create(registryKey, key);
    }

    /** Registers every entry collected so far. The bus is unused on Fabric. */
    public void register(@Nullable IEventBus bus) {
        if (registered) return;
        registered = true;
        entries.forEach(this::registerEntry);
    }

    @SuppressWarnings("unchecked")
    private void registerEntry(DeferredHolder<T, ? extends T> holder, Supplier<? extends T> supplier) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        if (registry == null) throw new IllegalStateException("Unknown registry " + registryKey);
        Registry.register(registry, holder.getKey(), supplier.get());
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registryKey;
    }

    public Identifier getRegistryName() {
        return registryKey.identifier();
    }

    public String getNamespace() {
        return namespace;
    }

    public static class Items extends DeferredRegister<Item> {

        protected Items(String namespace) {
            super(Registries.ITEM, namespace);
        }
    }

    public static class Blocks extends DeferredRegister<Block> {

        protected Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }
    }
}
