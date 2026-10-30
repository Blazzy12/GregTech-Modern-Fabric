package com.gregtechceu.gtceu.compat.registries.datamaps;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.BiConsumer;

/**
 * A NeoForge data map type. On Fabric the vanilla equivalents are data components, so each type applies its
 * value directly to the registered object.
 */
public record DataMapType<R, T>(ResourceKey<? extends Registry<R>> registryKey, Identifier id,
                                BiConsumer<R, T> applier) {

    public void apply(R object, T value) {
        applier.accept(object, value);
    }
}
