package com.gregtechceu.gtceu.compat.client.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.client.color.BlockColor;
import com.gregtechceu.gtceu.compat.client.color.ColorHandlers;
import com.gregtechceu.gtceu.compat.client.color.ItemColor;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.world.level.ItemLike;

public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {

    public static class Block extends RegisterColorHandlersEvent {

        public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {
            ColorHandlers.registerBlock(color, blocks);
        }
    }

    public static class Item extends RegisterColorHandlersEvent {

        public void register(ItemColor color, ItemLike... items) {
            ColorHandlers.registerItem(color, items);
        }
    }
}
