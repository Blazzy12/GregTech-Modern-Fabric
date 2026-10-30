package com.gregtechceu.gtceu.compat.event.entity.living;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LivingEquipmentChangeEvent extends Event {

    private final LivingEntity entity;
    private final EquipmentSlot slot;
    private final ItemStack from;
    private final ItemStack to;

    public LivingEquipmentChangeEvent(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
        this.entity = entity;
        this.slot = slot;
        this.from = from;
        this.to = to;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public ItemStack getFrom() {
        return from;
    }

    public ItemStack getTo() {
        return to;
    }
}
