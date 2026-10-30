package com.gregtechceu.gtceu.compat.items.wrapper;

import net.minecraft.world.entity.player.Inventory;

public class PlayerMainInvWrapper extends RangedWrapper {

    private final Inventory inventoryPlayer;

    public PlayerMainInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), 0, Inventory.INVENTORY_SIZE);
        this.inventoryPlayer = inv;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }
}
