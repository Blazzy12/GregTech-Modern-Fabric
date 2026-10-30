package com.gregtechceu.gtceu.compat.event.entity;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class RegisterSpawnPlacementsEvent extends Event implements IModBusEvent {

    public <T extends Mob> void register(EntityType<T> entityType, SpawnPlacementType placementType,
                                         Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate,
                                         Operation operation) {
        SpawnPlacements.register(entityType, placementType, heightmap, predicate);
    }

    public enum Operation {
        REPLACE,
        OR,
        AND
    }
}
