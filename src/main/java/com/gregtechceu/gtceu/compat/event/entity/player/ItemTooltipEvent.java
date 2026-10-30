package com.gregtechceu.gtceu.compat.event.entity.player;

import com.gregtechceu.gtceu.compat.bus.Event;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemTooltipEvent extends Event {

    private final ItemStack itemStack;
    private final Player entity;
    private final List<Component> toolTip;
    private final TooltipFlag flags;
    private final Item.TooltipContext context;

    public ItemTooltipEvent(ItemStack itemStack, Player entity, List<Component> toolTip, TooltipFlag flags, Item.TooltipContext context) {
        this.itemStack = itemStack;
        this.entity = entity;
        this.toolTip = toolTip;
        this.flags = flags;
        this.context = context;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Player getEntity() {
        return entity;
    }

    public List<Component> getToolTip() {
        return toolTip;
    }

    public TooltipFlag getFlags() {
        return flags;
    }

    public Item.TooltipContext getContext() {
        return context;
    }
}
