package com.gregtechceu.gtceu.compat.event.tick;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.entity.Entity;

public abstract class EntityTickEvent extends Event {

    private final Entity entity;

    public EntityTickEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public static class Pre extends EntityTickEvent implements ICancellableEvent {

        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static class Post extends EntityTickEvent {

        public Post(Entity entity) {
            super(entity);
        }
    }
}
