package com.gregtechceu.gtceu.compat.common;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.Optional;

/**
 * Either a resolved holder or just a registry key, resolved later against a lookup. Replaces the vanilla class
 * removed in 26.x; keys compare equal whether or not they are resolved.
 */
public record EitherHolder<T>(Optional<Holder<T>> holder, ResourceKey<T> key) {

    public EitherHolder(Holder<T> holder) {
        this(Optional.of(holder), holder.unwrapKey().orElseThrow());
    }

    public EitherHolder(ResourceKey<T> key) {
        this(Optional.empty(), key);
    }

    public static <T> Codec<EitherHolder<T>> codec(ResourceKey<Registry<T>> registryKey, Codec<Holder<T>> codec) {
        return Codec.either(codec, ResourceKey.codec(registryKey)).xmap(
                either -> either.map(EitherHolder::new, EitherHolder::new),
                holder -> holder.holder.<Either<Holder<T>, ResourceKey<T>>>map(Either::left)
                        .orElseGet(() -> Either.right(holder.key)));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(
                                                                                     ResourceKey<Registry<T>> registryKey,
                                                                                     StreamCodec<RegistryFriendlyByteBuf, Holder<T>> streamCodec) {
        return ByteBufCodecs.either(streamCodec, ResourceKey.streamCodec(registryKey)).map(
                either -> either.map(EitherHolder::new, EitherHolder::new),
                holder -> holder.holder.<Either<Holder<T>, ResourceKey<T>>>map(Either::left)
                        .orElseGet(() -> Either.right(holder.key)));
    }

    public Optional<Holder<T>> unwrap(HolderLookup.Provider registries) {
        return holder.isPresent() ? holder : registries.get(key).map(h -> h);
    }

    public Optional<T> unwrap(Registry<T> registry) {
        return holder.map(Holder::value).or(() -> registry.getOptional(key));
    }

    public Optional<ResourceKey<T>> asKey() {
        return Optional.of(key);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EitherHolder<?> other && other.key.equals(key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
