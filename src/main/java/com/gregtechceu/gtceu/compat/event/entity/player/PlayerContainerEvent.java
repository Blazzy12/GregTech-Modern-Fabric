package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class PlayerContainerEvent extends Event {

    private final Player entity;
    private final AbstractContainerMenu container;

    public PlayerContainerEvent(Player entity, AbstractContainerMenu container) {
        this.entity = entity;
        this.container = container;
    }

    public Player getEntity() {
        return entity;
    }

    public AbstractContainerMenu getContainer() {
        return container;
    }

    public static class Open extends PlayerContainerEvent {

        public Open(Player entity, AbstractContainerMenu container) {
            super(entity, container);
        }
    }

    public static class Close extends PlayerContainerEvent {

        public Close(Player entity, AbstractContainerMenu container) {
            super(entity, container);
        }
    }
}
