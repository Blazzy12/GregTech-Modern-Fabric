package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.entity.LivingEntity;

public class LivingEvent extends Event {

    private final LivingEntity entity;

    public LivingEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public static class LivingJumpEvent extends LivingEvent {

        public LivingJumpEvent(LivingEntity entity) {
            super(entity);
        }
    }
}
