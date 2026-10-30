package com.gregtechceu.gtceu.compat.items.wrapper;

import com.gregtechceu.gtceu.compat.items.IItemHandlerModifiable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Exposes an item handler as a recipe input. */
public class RecipeWrapper implements RecipeInput {

    protected final IItemHandlerModifiable inv;

    public RecipeWrapper(IItemHandlerModifiable inv) {
        this.inv = inv;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public int size() {
        return inv.getSlots();
    }
}
