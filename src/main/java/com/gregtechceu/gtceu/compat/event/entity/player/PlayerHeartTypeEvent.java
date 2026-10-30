package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;

public class PlayerHeartTypeEvent extends Event {

    private final Player entity;
    private final Hud.HeartType originalType;
    private Hud.HeartType type;

    public PlayerHeartTypeEvent(Player entity, Hud.HeartType originalType, Hud.HeartType type) {
        this.entity = entity;
        this.originalType = originalType;
        this.type = type;
    }

    public Player getEntity() {
        return entity;
    }

    public Hud.HeartType getOriginalType() {
        return originalType;
    }

    public Hud.HeartType getType() {
        return type;
    }

    public void setType(Hud.HeartType type) {
        this.type = type;
    }
}
