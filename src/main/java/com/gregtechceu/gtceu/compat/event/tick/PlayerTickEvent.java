package com.gregtechceu.gtceu.compat.event.tick;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerTickEvent extends Event {

    private final Player entity;

    public PlayerTickEvent(Player entity) {
        this.entity = entity;
    }

    public Player getEntity() {
        return entity;
    }

    public static class Pre extends PlayerTickEvent {

        public Pre(Player entity) {
            super(entity);
        }
    }

    public static class Post extends PlayerTickEvent {

        public Post(Player entity) {
            super(entity);
        }
    }
}
