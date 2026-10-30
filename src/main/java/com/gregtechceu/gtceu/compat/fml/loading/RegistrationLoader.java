package com.gregtechceu.gtceu.compat.fml.loading;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.compat.capabilities.RegisterCapabilitiesEvent;
import com.gregtechceu.gtceu.compat.event.BlockEntityTypeAddBlocksEvent;
import com.gregtechceu.gtceu.compat.event.entity.EntityAttributeCreationEvent;
import com.gregtechceu.gtceu.compat.event.entity.RegisterSpawnPlacementsEvent;
import com.gregtechceu.gtceu.compat.fml.ModBuses;
import com.gregtechceu.gtceu.compat.registries.DataPackRegistryEvent;
import com.gregtechceu.gtceu.compat.registries.NewRegistryEvent;
import com.gregtechceu.gtceu.compat.registries.RegisterEvent;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Replays NeoForge's registration lifecycle on the mod bus during Fabric initialisation, where every registry is
 * still open: new registries, datapack registries, then one {@link RegisterEvent} per registry in NeoForge's
 * order with GT's registries first, then the follow-up events.
 */
public final class RegistrationLoader {

    private RegistrationLoader() {}

    public static void run() {
        ModBuses.MOD_BUS.post(new NewRegistryEvent());
        ModBuses.MOD_BUS.post(new DataPackRegistryEvent.NewRegistry());

        Set<Identifier> posted = new java.util.HashSet<>();
        for (Identifier name : registrationOrder()) {
            Identifier lookup = name.equals(GTRegistries.Keys.RECIPE_TYPE.identifier()) ?
                    Registries.RECIPE_TYPE.identifier() : name;
            // GT's recipe type key is the vanilla registry, which must only be posted once
            if (!posted.add(lookup)) continue;
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(lookup);
            if (registry == null) continue;
            ModBuses.MOD_BUS.post(new RegisterEvent(ResourceKey.createRegistryKey(lookup), registry));
        }

        ModBuses.MOD_BUS.post(new EntityAttributeCreationEvent());
        ModBuses.MOD_BUS.post(new RegisterSpawnPlacementsEvent());
        ModBuses.MOD_BUS.post(new BlockEntityTypeAddBlocksEvent());
        ModBuses.MOD_BUS.post(new RegisterCapabilitiesEvent());
    }

    private static Set<Identifier> registrationOrder() {
        Set<Identifier> ordered = new LinkedHashSet<>(GTRegistries.getRegistryOrder());
        ordered.add(Registries.ATTRIBUTE.identifier());
        ordered.add(Registries.DATA_COMPONENT_TYPE.identifier());
        // fluids before blocks, as in vanilla: material fluids create their blocks while fluids register
        ordered.add(Registries.FLUID.identifier());
        ordered.add(Registries.BLOCK.identifier());
        // entity types before items: boat and spawn-egg items take their EntityType in the constructor
        ordered.add(Registries.ENTITY_TYPE.identifier());
        ordered.add(Registries.ITEM.identifier());
        List<Identifier> rest = new ArrayList<>(BuiltInRegistries.REGISTRY.keySet());
        rest.sort(java.util.Comparator.comparing(Identifier::getNamespace).thenComparing(Identifier::getPath));
        ordered.addAll(rest);
        return ordered;
    }
}
