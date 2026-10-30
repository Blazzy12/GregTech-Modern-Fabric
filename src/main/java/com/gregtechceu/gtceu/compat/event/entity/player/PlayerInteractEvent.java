package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class PlayerInteractEvent extends Event {

    private final Player entity;
    private final InteractionHand hand;
    private final BlockPos pos;
    private final Direction face;

    public PlayerInteractEvent(Player entity, InteractionHand hand, BlockPos pos, Direction face) {
        this.entity = entity;
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public Player getEntity() {
        return entity;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getFace() {
        return face;
    }

    public static class LeftClickBlock extends PlayerInteractEvent implements ICancellableEvent {

        private final Action action;

        public LeftClickBlock(Player entity, InteractionHand hand, BlockPos pos, Direction face, Action action) {
            super(entity, hand, pos, face);
            this.action = action;
        }

        public Action getAction() {
            return action;
        }
    }

    public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent {

        public RightClickBlock(Player entity, InteractionHand hand, BlockPos pos, Direction face) {
            super(entity, hand, pos, face);
        }
    }

    public static class RightClickItem extends PlayerInteractEvent implements ICancellableEvent {

        public RightClickItem(Player entity, InteractionHand hand, BlockPos pos, Direction face) {
            super(entity, hand, pos, face);
        }
    }

    public static class EntityInteract extends PlayerInteractEvent implements ICancellableEvent {

        private final Entity target;

        public EntityInteract(Player entity, InteractionHand hand, BlockPos pos, Direction face, Entity target) {
            super(entity, hand, pos, face);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }

    private InteractionResult cancellationResult = InteractionResult.PASS;
    
    public ItemStack getItemStack() {
        return getEntity().getItemInHand(getHand());
    }
    
    public Level getLevel() {
        return getEntity().level();
    }
    
    public InteractionResult getCancellationResult() {
        return cancellationResult;
    }
    
    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }
    
    public enum Action {
        START,
        STOP,
        ABORT,
        CLIENT_HOLD
    }
}
