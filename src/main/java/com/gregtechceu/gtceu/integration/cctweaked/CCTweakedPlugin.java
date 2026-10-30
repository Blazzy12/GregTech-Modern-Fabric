package com.gregtechceu.gtceu.integration.cctweaked;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.compat.capabilities.BlockCapability;
import com.gregtechceu.gtceu.api.placeholder.*;
import com.gregtechceu.gtceu.api.placeholder.exceptions.NotSupportedException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.*;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.impl.Peripherals;

import net.minecraft.core.Direction;

import java.util.List;

public class CCTweakedPlugin {

    public static void init() {
        ComputerCraftAPI.registerGenericSource(new ControllablePeripheral());
        ComputerCraftAPI.registerGenericSource(new EnergyInfoPeripheral());
        ComputerCraftAPI.registerGenericSource(new TurbineMachinePeripheral());
        ComputerCraftAPI.registerGenericSource(new WorkablePeripheral());
        ComputerCraftAPI.registerGenericSource(new CoverHolderPeripheral());
        ComputerCraftAPI.registerGenericSource(new CentralMonitorPeripheral());
        ComputerCraftAPI.registerGenericSource(new MaintenancePeripheral());
        registerGenericCapability(GTCapability.CAPABILITY_CONTROLLABLE);
        registerGenericCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER);
        registerGenericCapability(GTCapability.CAPABILITY_WORKABLE);
        registerGenericCapability(GTCapability.CAPABILITY_COVERABLE);
    }

    /**
     * Lets CC's generic peripherals see a capability of a block, like {@code ForgeComputerCraftAPI
     * #registerGenericCapability}. CC on Fabric has no public API for this; {@code Peripherals} is its internal one.
     */
    private static void registerGenericCapability(BlockCapability<?, Direction> capability) {
        Peripherals.addGenericLookup((level, pos, state, blockEntity, side) -> level.getCapability(capability, pos,
                state, blockEntity, side));
    }

    public static void initPlaceholders() {
        GTRegistration.REGISTRATE.simple("buffer_text", GTRegistries.Keys.PLACEHOLDER,
                () -> new Placeholder("buffer_text") {

                    @Override
                    public MultiLineComponent apply(PlaceholderContext ctx,
                                                    List<MultiLineComponent> args) throws PlaceholderException {
                        PlaceholderUtils.checkArgs(args, 1);
                        if (!(ctx.cover() instanceof IPlaceholderInfoProviderCover cover))
                            throw new NotSupportedException();
                        int i = PlaceholderUtils.toInt(args.get(0));
                        PlaceholderUtils.checkRange("line number", 1, 100, i);
                        return MultiLineComponent.of(cover.getComputerCraftTextBuffer().get(i - 1));
                    }
                });
    }
}
