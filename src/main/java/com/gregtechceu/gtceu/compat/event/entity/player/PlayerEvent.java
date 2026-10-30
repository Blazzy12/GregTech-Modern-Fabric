package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerEvent extends Event {

    private final Player entity;

    public PlayerEvent(Player entity) {
        this.entity = entity;
    }

    public Player getEntity() {
        return entity;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {

        public PlayerLoggedInEvent(Player entity) {
            super(entity);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {

        public PlayerLoggedOutEvent(Player entity) {
            super(entity);
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {

        private final boolean endConquered;

        public PlayerRespawnEvent(Player entity, boolean endConquered) {
            super(entity);
            this.endConquered = endConquered;
        }

        public boolean getEndConquered() {
            return endConquered;
        }
    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent {

        private final ResourceKey<Level> from;
        private final ResourceKey<Level> to;

        public PlayerChangedDimensionEvent(Player entity, ResourceKey<Level> from, ResourceKey<Level> to) {
            super(entity);
            this.from = from;
            this.to = to;
        }

        public ResourceKey<Level> getFrom() {
            return from;
        }

        public ResourceKey<Level> getTo() {
            return to;
        }
    }

    public static class BreakSpeed extends PlayerEvent implements ICancellableEvent {

        private final BlockState state;
        private final float originalSpeed;
        private float newSpeed;
        private final Optional<BlockPos> position;

        public BreakSpeed(Player entity, BlockState state, float originalSpeed, float newSpeed, Optional<BlockPos> position) {
            super(entity);
            this.state = state;
            this.originalSpeed = originalSpeed;
            this.newSpeed = newSpeed;
            this.position = position;
        }

        public BlockState getState() {
            return state;
        }

        public float getOriginalSpeed() {
            return originalSpeed;
        }

        public float getNewSpeed() {
            return newSpeed;
        }

        public void setNewSpeed(float newSpeed) {
            this.newSpeed = newSpeed;
        }

        public Optional<BlockPos> getPosition() {
            return position;
        }
    }

    public static class Clone extends PlayerEvent {

        private final Player original;
        private final boolean wasDeath;

        public Clone(Player entity, Player original, boolean wasDeath) {
            super(entity);
            this.original = original;
            this.wasDeath = wasDeath;
        }

        public Player getOriginal() {
            return original;
        }

        public boolean getWasDeath() {
            return wasDeath;
        }
    }
}
