package com.gregtechceu.gtceu.compat.client.color;

import net.minecraft.world.item.ItemStack;

/** Pre-26.x item color handler, bridged to the {@code gtceu:legacy_color} item tint source. */
@FunctionalInterface
public interface ItemColor {

    int getColor(ItemStack stack, int tintIndex);
}
