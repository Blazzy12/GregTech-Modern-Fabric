package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypeAddBlocksEvent extends Event implements IModBusEvent {

    public void modify(BlockEntityType<?> type, Block... blocks) {
        for (Block block : blocks) type.addValidBlock(block);
    }
}
