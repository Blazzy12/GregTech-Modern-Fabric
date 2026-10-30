package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingIncomingDamageEvent extends Event implements ICancellableEvent {

    private final LivingEntity entity;
    private final DamageSource source;
    private float amount;

    public LivingIncomingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
