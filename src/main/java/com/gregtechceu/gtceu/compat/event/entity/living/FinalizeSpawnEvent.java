package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

public class FinalizeSpawnEvent extends Event implements ICancellableEvent {

    private final Mob entity;
    private final ServerLevelAccessor level;
    private final EntitySpawnReason spawnType;

    public FinalizeSpawnEvent(Mob entity, ServerLevelAccessor level, EntitySpawnReason spawnType) {
        this.entity = entity;
        this.level = level;
        this.spawnType = spawnType;
    }

    public Mob getEntity() {
        return entity;
    }

    public ServerLevelAccessor getLevel() {
        return level;
    }

    public EntitySpawnReason getSpawnType() {
        return spawnType;
    }
}
