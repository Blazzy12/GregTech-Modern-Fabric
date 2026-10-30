package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class LivingEntityUseItemEvent extends Event {

    private final LivingEntity entity;
    private final ItemStack item;
    private int duration;

    public LivingEntityUseItemEvent(LivingEntity entity, ItemStack item, int duration) {
        this.entity = entity;
        this.item = item;
        this.duration = duration;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static class Finish extends LivingEntityUseItemEvent {

        private ItemStack resultStack;

        public Finish(LivingEntity entity, ItemStack item, int duration, ItemStack resultStack) {
            super(entity, item, duration);
            this.resultStack = resultStack;
        }

        public ItemStack getResultStack() {
            return resultStack;
        }

        public void setResultStack(ItemStack resultStack) {
            this.resultStack = resultStack;
        }
    }
}
