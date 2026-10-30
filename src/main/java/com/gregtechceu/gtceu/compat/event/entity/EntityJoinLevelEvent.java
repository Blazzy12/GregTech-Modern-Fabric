package com.gregtechceu.gtceu.compat.event.entity;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityJoinLevelEvent extends Event implements ICancellableEvent {

    private final Entity entity;
    private final Level level;
    private final boolean loadedFromDisk;

    public EntityJoinLevelEvent(Entity entity, Level level, boolean loadedFromDisk) {
        this.entity = entity;
        this.level = level;
        this.loadedFromDisk = loadedFromDisk;
    }

    public Entity getEntity() {
        return entity;
    }

    public Level getLevel() {
        return level;
    }

    public boolean getLoadedFromDisk() {
        return loadedFromDisk;
    }
}
