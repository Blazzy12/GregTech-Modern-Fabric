package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.bus.ICancellableEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/** Fired when the anvil computes its result. Cancelling leaves the output empty. */
public class AnvilUpdateEvent extends Event implements ICancellableEvent {

    private final ItemStack left;
    private final ItemStack right;
    private final @Nullable String name;
    private final Player player;

    public AnvilUpdateEvent(ItemStack left, ItemStack right, @Nullable String name, Player player) {
        this.left = left;
        this.right = right;
        this.name = name;
        this.player = player;
    }

    public ItemStack getLeft() {
        return left;
    }

    public ItemStack getRight() {
        return right;
    }

    public @Nullable String getName() {
        return name;
    }

    public Player getPlayer() {
        return player;
    }
}
