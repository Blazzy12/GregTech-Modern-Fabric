package com.gregtechceu.gtceu.compat.items.wrapper;

import net.minecraft.world.entity.player.Inventory;

public class PlayerOffhandInvWrapper extends RangedWrapper {

    private final Inventory inventoryPlayer;

    public PlayerOffhandInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), Inventory.SLOT_OFFHAND, Inventory.SLOT_OFFHAND + 1);
        this.inventoryPlayer = inv;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }
}
