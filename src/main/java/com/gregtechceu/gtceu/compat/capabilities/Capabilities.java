package com.gregtechceu.gtceu.compat.capabilities;

import com.gregtechceu.gtceu.compat.energy.IEnergyStorage;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandlerItem;
import com.gregtechceu.gtceu.compat.items.IItemHandler;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

public final class Capabilities {

    private Capabilities() {}

    public static final class ItemHandler {

        public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK = BlockCapability
                .createSided(id("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Void> ENTITY = EntityCapability
                .createVoid(id("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability
                .createSided(id("item_handler_automation"), IItemHandler.class);
        public static final ItemCapability<IItemHandler, @Nullable Void> ITEM = ItemCapability
                .createVoid(id("item_handler"), IItemHandler.class);

        private ItemHandler() {}
    }

    public static final class FluidHandler {

        public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK = BlockCapability
                .createSided(id("fluid_handler"), IFluidHandler.class);
        public static final EntityCapability<IFluidHandler, @Nullable Direction> ENTITY = EntityCapability
                .createSided(id("fluid_handler"), IFluidHandler.class);
        public static final ItemCapability<IFluidHandlerItem, @Nullable Void> ITEM = ItemCapability
                .createVoid(id("fluid_handler"), IFluidHandlerItem.class);

        private FluidHandler() {}
    }

    public static final class EnergyStorage {

        public static final BlockCapability<IEnergyStorage, @Nullable Direction> BLOCK = BlockCapability
                .createSided(id("energy"), IEnergyStorage.class);
        public static final EntityCapability<IEnergyStorage, @Nullable Direction> ENTITY = EntityCapability
                .createSided(id("energy"), IEnergyStorage.class);
        public static final ItemCapability<IEnergyStorage, @Nullable Void> ITEM = ItemCapability
                .createVoid(id("energy"), IEnergyStorage.class);

        private EnergyStorage() {}
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("gtceu_compat", path);
    }
}
