package com.gregtechceu.gtceu.compat.capabilities;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Posted once on the mod bus after registries are populated. Registrations go straight to the Fabric lookups.
 */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {

    @SuppressWarnings("unchecked")
    public <T, C, BE extends BlockEntity> void registerBlockEntity(BlockCapability<T, C> capability,
                                                                   BlockEntityType<BE> type,
                                                                   ICapabilityProvider<? super BE, C, T> provider) {
        capability.lookup().registerForBlockEntities(
                (be, context) -> provider.getCapability((BE) be, context), type);
    }

    public <T, C> void registerBlock(BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider,
                                     Block... blocks) {
        capability.lookup().registerForBlocks(provider::getCapability, blocks);
    }

    public <T, C> void registerItem(ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider,
                                    ItemLike... items) {
        capability.lookup().registerForItems(provider::getCapability, items);
    }

    @SuppressWarnings("unchecked")
    public <T, C, E extends Entity> void registerEntity(EntityCapability<T, C> capability, EntityType<E> type,
                                                        ICapabilityProvider<? super E, C, T> provider) {
        capability.lookup().registerForTypes((entity, context) -> provider.getCapability((E) entity, context), type);
    }
}
