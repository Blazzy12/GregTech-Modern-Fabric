package com.gregtechceu.gtceu.compat.client.extensions.common;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fluids.FluidType;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class RegisterClientExtensionsEvent extends Event implements IModBusEvent {

    public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType... types) {
        for (FluidType type : types) ClientExtensionsManager.FLUID_TYPE_EXTENSIONS.put(type, extensions);
    }

    public void registerItem(IClientItemExtensions extensions, ItemLike... items) {
        for (ItemLike item : items) ClientExtensionsManager.ITEM_EXTENSIONS.put(item.asItem(), extensions);
    }

    public void registerBlock(IClientBlockExtensions extensions, Block... blocks) {
        for (Block block : blocks) ClientExtensionsManager.BLOCK_EXTENSIONS.put(block, extensions);
    }
}
