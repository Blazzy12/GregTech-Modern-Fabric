package com.gregtechceu.gtceu.compat.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge's item decorator: draws over an item in GUI slots (durability and charge bars). GT invokes these itself
 * from its item rendering hook.
 *
 * @return whether render state changed
 */
@FunctionalInterface
public interface IItemDecorator {

    boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset);
}
