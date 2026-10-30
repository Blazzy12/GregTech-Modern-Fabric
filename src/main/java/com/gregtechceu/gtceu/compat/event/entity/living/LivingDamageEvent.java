package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public abstract class LivingDamageEvent extends Event {

    private final LivingEntity entity;
    private final DamageSource source;

    public LivingDamageEvent(LivingEntity entity, DamageSource source) {
        this.entity = entity;
        this.source = source;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public static class Post extends LivingDamageEvent {

        private final float newDamage;
        private final float originalDamage;

        public Post(LivingEntity entity, DamageSource source, float newDamage, float originalDamage) {
            super(entity, source);
            this.newDamage = newDamage;
            this.originalDamage = originalDamage;
        }

        public float getNewDamage() {
            return newDamage;
        }

        public float getOriginalDamage() {
            return originalDamage;
        }
    }
}
