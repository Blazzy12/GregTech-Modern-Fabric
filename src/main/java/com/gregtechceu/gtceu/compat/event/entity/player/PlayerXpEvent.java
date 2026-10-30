package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerXpEvent extends Event {

    private final Player entity;

    public PlayerXpEvent(Player entity) {
        this.entity = entity;
    }

    public Player getEntity() {
        return entity;
    }

    public static class PickupXp extends PlayerXpEvent implements ICancellableEvent {

        private final ExperienceOrb orb;

        public PickupXp(Player entity, ExperienceOrb orb) {
            super(entity);
            this.orb = orb;
        }

        public ExperienceOrb getOrb() {
            return orb;
        }
    }

    public static class XpChange extends PlayerXpEvent implements ICancellableEvent {

        private int amount;

        public XpChange(Player entity, int amount) {
            super(entity);
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
