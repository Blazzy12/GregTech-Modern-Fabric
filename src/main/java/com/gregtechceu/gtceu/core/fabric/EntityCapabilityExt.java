package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.capabilities.EntityCapability;

import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;

/** Injected into {@link Entity}: NeoForge-style capability queries backed by Fabric API Lookup. */
public interface EntityCapabilityExt {

    default <T> @Nullable T getCapability(EntityCapability<T, Void> capability) {
        return capability.getCapability((Entity) this, null);
    }

    default <T, C> @Nullable T getCapability(EntityCapability<T, C> capability, C context) {
        return capability.getCapability((Entity) this, context);
    }

    /** NeoForge's saved per-entity compound, kept in a persistent Fabric attachment. Mutate it in place. */
    default net.minecraft.nbt.CompoundTag getPersistentData() {
        return ((Entity) this).getAttachedOrCreate(com.gregtechceu.gtceu.compat.common.NeoForgeMod.PERSISTENT_DATA);
    }
}
