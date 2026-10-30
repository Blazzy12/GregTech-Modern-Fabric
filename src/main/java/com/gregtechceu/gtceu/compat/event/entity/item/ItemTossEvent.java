package com.gregtechceu.gtceu.compat.event.entity.item;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class ItemTossEvent extends Event implements ICancellableEvent {

    private final ItemEntity entity;
    private final Player player;

    public ItemTossEvent(ItemEntity entity, Player player) {
        this.entity = entity;
        this.player = player;
    }

    public ItemEntity getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }
}
