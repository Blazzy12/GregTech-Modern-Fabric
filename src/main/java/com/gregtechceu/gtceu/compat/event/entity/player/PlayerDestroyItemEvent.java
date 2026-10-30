package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerDestroyItemEvent extends Event {

    private final Player entity;
    private final ItemStack original;
    private final InteractionHand hand;

    public PlayerDestroyItemEvent(Player entity, ItemStack original, InteractionHand hand) {
        this.entity = entity;
        this.original = original;
        this.hand = hand;
    }

    public Player getEntity() {
        return entity;
    }

    public ItemStack getOriginal() {
        return original;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
