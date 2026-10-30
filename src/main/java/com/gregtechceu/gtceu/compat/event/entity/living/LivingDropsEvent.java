package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import java.util.Collection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public class LivingDropsEvent extends Event implements ICancellableEvent {

    private final LivingEntity entity;
    private final DamageSource source;
    private final Collection<ItemEntity> drops;
    private final boolean recentlyHit;

    public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, boolean recentlyHit) {
        this.entity = entity;
        this.source = source;
        this.drops = drops;
        this.recentlyHit = recentlyHit;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public Collection<ItemEntity> getDrops() {
        return drops;
    }

    public boolean getRecentlyHit() {
        return recentlyHit;
    }
}
