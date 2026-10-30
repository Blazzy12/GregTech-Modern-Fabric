package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.entity.LivingEntity;

public class LivingFallEvent extends Event implements ICancellableEvent {

    private final LivingEntity entity;
    private double distance;
    private float damageMultiplier;

    public LivingFallEvent(LivingEntity entity, double distance, float damageMultiplier) {
        this.entity = entity;
        this.distance = distance;
        this.damageMultiplier = damageMultiplier;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }
}
