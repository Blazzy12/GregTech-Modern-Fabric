package com.gregtechceu.gtceu.compat.items.wrapper;

import net.minecraft.world.entity.player.Inventory;

public class PlayerArmorInvWrapper extends RangedWrapper {

    private final Inventory inventoryPlayer;

    public PlayerArmorInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), Inventory.INVENTORY_SIZE, Inventory.INVENTORY_SIZE + 4);
        this.inventoryPlayer = inv;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }
}
