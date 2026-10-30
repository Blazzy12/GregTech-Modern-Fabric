package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.integration.jade.provider.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import snownee.jade.api.harvest.ToolTier;
import snownee.jade.api.harvest.ToolType;
import snownee.jade.api.harvest.ToolTypeRegistry;
import snownee.jade.api.*;

import java.util.List;
import java.util.Objects;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        register(registration, new ElectricContainerBlockProvider(),
                new WorkableBlockProvider(),
                new ControllableBlockProvider(),
                new RecipeLogicProvider(),
                new ParallelProvider(),
                new RecipeOutputProvider(),
                new MultiblockStructureProvider(),
                new MaintenanceBlockProvider(),
                new ExhaustVentBlockProvider(),
                new SteamBoilerBlockProvider(),
                new AutoOutputBlockProvider(),
                new CableBlockProvider(),
                new MachineModeProvider(),
                new StainedColorProvider(),
                new HazardCleanerBlockProvider(),
                new TransformerBlockProvider(),
                new DiodeModeProvider(),
                new PrimitivePumpBlockProvider(),
                new DataBankBlockProvider(),
                new EnergyConverterModeProvider(),
                new BatteryStorageInfoProvider(),
                new LDPEndpointProvider(),
                new ProgrammableCircuitProvider());

        registration.registerItemStorage(GTItemStorageProvider.INSTANCE, MetaMachine.class);
        registration.registerFluidStorage(GTFluidStorageProvider.INSTANCE, MetaMachine.class);
        registration.registerFluidStorage(FluidPipeStorageProvider.INSTANCE, FluidPipeBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        register(registration,
                new ElectricContainerBlockProvider(),
                new WorkableBlockProvider(),
                new ControllableBlockProvider(),
                new RecipeLogicProvider(),
                new ParallelProvider(),
                new RecipeOutputProvider(),
                new MultiblockStructureProvider(),
                new MaintenanceBlockProvider(),
                new ExhaustVentBlockProvider(),
                new SteamBoilerBlockProvider(),
                new AutoOutputBlockProvider(),
                new MachineModeProvider(),
                new StainedColorProvider(),
                new HazardCleanerBlockProvider(),
                new TransformerBlockProvider(),
                new DiodeModeProvider(),
                new PrimitivePumpBlockProvider(),
                new DataBankBlockProvider(),
                new LDPEndpointProvider(),
                new EnergyConverterModeProvider(),
                new BatteryStorageInfoProvider(),
                new CableBlockProvider(),
                new ProgrammableCircuitProvider());

        registration.addHarvestPlugin(GTJadePlugin::registerHarvestTools);
        registration.registerItemStorageClient(GTItemStorageProvider.INSTANCE);
        registration.registerFluidStorageClient(GTFluidStorageProvider.INSTANCE);
        registration.registerFluidStorageClient(FluidPipeStorageProvider.INSTANCE);
    }

    @SafeVarargs
    public static void register(IWailaCommonRegistration reg, IServerDataProvider<BlockAccessor>... providers) {
        for (var provider : providers) {
            Class<? extends BlockEntity> clazz = BlockEntity.class;

            if (provider instanceof MachineInfoProvider<?, ?> machineInfoProvider)
                clazz = machineInfoProvider.machineType;
            if (provider instanceof MachineTraitProvider<?, ?>) clazz = MetaMachine.class;

            // Jade wants data providers that are not also component providers
            reg.registerBlockDataProvider(new ServerData(provider), clazz);
        }
    }

    public static void register(IWailaClientRegistration reg, IBlockComponentProvider... providers) {
        for (var provider : providers) {
            Class<? extends Block> clazz = Block.class;
            if (provider instanceof MachineInfoProvider<?, ?> || provider instanceof MachineTraitProvider<?, ?>) {
                clazz = MetaMachineBlock.class;
            }
            reg.registerBlockComponent(provider, clazz);
        }
    }

    /** The server half of a GT provider, which draws its own tooltip on the client. */
    private record ServerData(IServerDataProvider<BlockAccessor> provider) implements IServerDataProvider<BlockAccessor> {

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            provider.appendServerData(data, accessor);
        }

        @Override
        public boolean shouldRequestData(BlockAccessor accessor) {
            return provider.shouldRequestData(accessor);
        }

        @Override
        public Identifier getUid() {
            return provider.getUid();
        }

        @Override
        public int getDefaultPriority() {
            return provider.getDefaultPriority();
        }
    }

    /** GT's tool types for Jade's harvest info: one tool type per GT tool, with each of its items as a tier. */
    private static void registerHarvestTools(ToolTypeRegistry registry) {
        GTMaterialItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
            boolean hasCustomHarvestTag = type.harvestTags.stream()
                    .anyMatch(tag -> !tag.location().getNamespace().equals(Identifier.DEFAULT_NAMESPACE));
            if ((type.toolDefinition.getTool().rules().isEmpty() && !hasCustomHarvestTag) || map.isEmpty()) return;

            List<Item> tools = map
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(ItemProviderEntry::isBound)
                    .map(ItemProviderEntry::asItem)
                    .toList();
            if (tools.isEmpty()) return;
            ToolType toolType = registry.type(ToolType.of(GTCEu.id(type.name)));
            for (Item tool : tools) {
                toolType.addTier(ToolTier.item(tool));
            }
        });
    }
}
