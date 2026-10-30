package com.gregtechceu.gtceu.core.fabric;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Blocks that add lines to their item's tooltip. Called from the block item's tooltip. */
public interface IBlockTooltip {

    void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag);
}
