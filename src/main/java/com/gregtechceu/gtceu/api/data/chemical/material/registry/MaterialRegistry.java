package com.gregtechceu.gtceu.api.data.chemical.material.registry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.core.mixins.MappedRegistryAccessor;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import com.mojang.serialization.Lifecycle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public final class MaterialRegistry extends MappedRegistry<Material> {

    @Getter
    private boolean registryClosed = false;

    public MaterialRegistry(ResourceKey<Registry<Material>> key) {
        super(key, Lifecycle.stable());
    }

    public Material register(Material material) {
        return register(material.getResourceLocation(), material);
    }

    private Material register(Identifier id, Material material) {
        this.register(ResourceKey.create(this.key(), id), material, RegistrationInfo.BUILT_IN);
        return material;
    }

    @Override
    public Identifier getKey(Material material) {
        return material.getResourceLocation();
    }

    @Override
    public Holder.@NotNull Reference<Material> register(@NotNull ResourceKey<Material> key, @NotNull Material value,
                                                        @NotNull RegistrationInfo registrationInfo) {
        if (registryClosed) {
            throw new IllegalStateException(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the RegisterEvent. Skipping material %s..."
                            .formatted(key.identifier()));
        }
        return super.register(key, value, registrationInfo);
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public void close() {
        registryClosed = true;
    }
}
