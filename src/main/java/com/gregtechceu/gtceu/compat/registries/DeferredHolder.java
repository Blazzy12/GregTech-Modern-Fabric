package com.gregtechceu.gtceu.compat.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A lazy reference to a registry entry. {@link Holder} is sealed in 26.x, but {@link Holder.Reference} is
 * non-sealed, so this extends it and delegates every query to the real reference once the entry is registered.
 * <p>
 * Equality is by registry key. Vanilla references compare by identity, so store {@link #getDelegate()} in
 * vanilla data structures that compare holders.
 */
public class DeferredHolder<R, T extends R> extends Holder.Reference<R> implements Supplier<T> {

    private static final HolderOwner<Object> UNBOUND_OWNER = new HolderOwner<>() {};

    protected final ResourceKey<R> key;
    private @Nullable Holder.Reference<R> holder;

    @SuppressWarnings("unchecked")
    protected DeferredHolder(ResourceKey<R> key) {
        super(Type.STAND_ALONE, (HolderOwner<R>) (Object) UNBOUND_OWNER, key, null);
        this.key = key;
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey,
                                                               Identifier valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    public static <R, T extends R> DeferredHolder<R, T> create(Identifier registryName, Identifier valueName) {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    @SuppressWarnings("unchecked")
    protected @Nullable Registry<R> getRegistry() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
    }

    protected final void bind(boolean throwOnMissing) {
        if (holder != null) return;
        Registry<R> registry = getRegistry();
        if (registry != null) {
            holder = registry.get(key).orElse(null);
        } else if (throwOnMissing) {
            throw new IllegalStateException("Registry not present for " + this + ": " + key.registry());
        }
        if (holder == null && throwOnMissing) {
            throw new IllegalStateException("Registry entry not present: " + key);
        }
    }

    /** The real registry reference. */
    public Holder<R> getDelegate() {
        bind(false);
        return holder != null ? holder : this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T value() {
        bind(true);
        return (T) holder.value();
    }

    @Override
    public T get() {
        return value();
    }

    public Optional<T> asOptional() {
        return isBound() ? Optional.of(value()) : Optional.empty();
    }

    public Identifier getId() {
        return key.identifier();
    }

    public ResourceKey<R> getKey() {
        return key;
    }

    @Override
    public ResourceKey<R> key() {
        return key;
    }

    @Override
    public boolean isBound() {
        bind(false);
        return holder != null && holder.isBound();
    }

    @Override
    public boolean areComponentsBound() {
        bind(false);
        return holder != null && holder.areComponentsBound();
    }

    @Override
    public boolean is(Identifier id) {
        return id.equals(key.identifier());
    }

    @Override
    public boolean is(ResourceKey<R> key) {
        return key == this.key || key.equals(this.key);
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        bind(false);
        return holder != null && holder.is(tag);
    }

    @Override
    public boolean is(Holder<R> other) {
        return other.unwrapKey().map(this::is).orElse(false);
    }

    @Override
    public Stream<TagKey<R>> tags() {
        bind(false);
        return holder != null ? holder.tags() : Stream.empty();
    }

    @Override
    public DataComponentMap components() {
        bind(true);
        return holder.components();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        bind(false);
        return holder != null && holder.canSerializeIn(owner);
    }

    public String getRegisteredName() {
        return key.identifier().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DeferredHolder<?, ?> other) return other.key.equals(key);
        return holder != null && obj == holder;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "DeferredHolder{" + key + "}";
    }
}
