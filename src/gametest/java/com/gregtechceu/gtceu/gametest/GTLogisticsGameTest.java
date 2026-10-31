package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveWorkableMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;
import com.gregtechceu.gtceu.compat.capabilities.Capabilities;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Moving things around: a hopper into an item pipe into a chest, a pump cover through a fluid pipe between tanks, a
 * conveyor cover out of a super chest, a steam boiler powering a steam macerator, and a coke oven running a recipe.
 * All set up side by side and checked after one wait.
 */
public class GTLogisticsGameTest implements FabricClientGameTest {

    private static final BlockPos HOPPER = new BlockPos(20, -60, 10), ITEM_PIPE = HOPPER.east(),
            PIPE_CHEST = ITEM_PIPE.east();
    private static final BlockPos TANK_A = new BlockPos(20, -60, 14), FLUID_PIPE = TANK_A.east(),
            TANK_B = FLUID_PIPE.east();
    private static final BlockPos SUPER_CHEST = new BlockPos(20, -60, 18), CONVEYOR_CHEST = SUPER_CHEST.east();
    private static final BlockPos BOILER = new BlockPos(20, -60, 22), STEAM_MACERATOR = BOILER.east();
    private static final BlockPos COKE_OVEN = new BlockPos(26, -59, 10);

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!GTTests.enabled("logistics")) return;
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            singleplayer.getConnection().waitForChunksRender();
            server.runCommand("tp @p 22 -60 4");
            context.waitTicks(10);

            server.runOnServer(s -> {
                ServerLevel level = s.overworld();
                setBlock(level, HOPPER, "minecraft:hopper[facing=east]");
                level.setBlockAndUpdate(ITEM_PIPE, GTMaterialBlocks.ITEM_PIPE_BLOCKS
                        .get(TagPrefix.pipeNormalItem.value(), GTMaterials.Tin.value()).get().defaultBlockState());
                setBlock(level, PIPE_CHEST, "minecraft:chest");

                setBlock(level, TANK_A, "gtceu:lv_super_tank");
                level.setBlockAndUpdate(FLUID_PIPE, GTMaterialBlocks.FLUID_PIPE_BLOCKS
                        .get(TagPrefix.pipeNormalFluid.value(), GTMaterials.Bronze.value()).get().defaultBlockState());
                setBlock(level, TANK_B, "gtceu:lv_super_tank");

                setBlock(level, SUPER_CHEST, "gtceu:lv_super_chest");
                setBlock(level, CONVEYOR_CHEST, "minecraft:chest");

                setBlock(level, BOILER, "gtceu:lp_steam_solid_boiler");
                setBlock(level, STEAM_MACERATOR, "gtceu:lp_steam_macerator");

            });
            // a hollow 3x3x3 of coke oven bricks with the controller in the middle of its north face
            int x = COKE_OVEN.getX(), y = COKE_OVEN.getY(), z = COKE_OVEN.getZ();
            server.runCommand("fill %d %d %d %d %d %d gtceu:coke_oven_bricks".formatted(x - 1, y - 1, z, x + 1,
                    y + 1, z + 2));
            server.runCommand("setblock %d %d %d air".formatted(x, y, z + 1));
            server.runCommand("setblock %d %d %d gtceu:coke_oven[facing=north]".formatted(x, y, z));
            context.waitTicks(20);

            server.runOnServer(s -> {
                ServerLevel level = s.overworld();
                pipe(level, ITEM_PIPE).setConnection(Direction.WEST, true, false);
                pipe(level, ITEM_PIPE).setConnection(Direction.EAST, true, false);
                if (level.getBlockEntity(HOPPER) instanceof Container hopper) {
                    hopper.setItem(0, new ItemStack(Items.COBBLESTONE, 16));
                }

                pipe(level, FLUID_PIPE).setConnection(Direction.WEST, true, false);
                pipe(level, FLUID_PIPE).setConnection(Direction.EAST, true, false);
                fluids(level, TANK_A).fill(new FluidStack(Fluids.WATER, 8000), IFluidHandler.FluidAction.EXECUTE);
                cover(level, TANK_A, Direction.EAST, "gtceu:lv_electric_pump", GTCovers.PUMPS[1].value());

                level.getCapability(Capabilities.ItemHandler.BLOCK, SUPER_CHEST, Direction.UP)
                        .insertItem(0, new ItemStack(Items.IRON_INGOT, 32), false);
                cover(level, SUPER_CHEST, Direction.EAST, "gtceu:lv_conveyor_module", GTCovers.CONVEYORS[1].value());
                // one more on a face the screenshot can see
                cover(level, TANK_A, Direction.WEST, "gtceu:lv_electric_pump", GTCovers.PUMPS[1].value());

                var boiler = (SteamSolidBoilerMachine) MetaMachine.getMachine(level, BOILER);
                boiler.waterTank.fill(new FluidStack(Fluids.WATER, 16000), IFluidHandler.FluidAction.EXECUTE);
                boiler.fuelHandler.insertItem(0, new ItemStack(Items.COAL, 16), false);
                GTMechanicsGameTest.setField(boiler, "currentTemperature", 300);
                ((SimpleSteamMachine) MetaMachine.getMachine(level, STEAM_MACERATOR)).importItems
                        .insertItem(0, new ItemStack(Items.IRON_ORE, 2), false);

                ((PrimitiveWorkableMachine) MetaMachine.getMachine(level, COKE_OVEN)).importItems
                        .insertItem(0, new ItemStack(Items.OAK_LOG, 2), false);
            });

            context.waitTicks(1000);
            context.takeScreenshot("gtceu_logistics");
            // close-ups: the connected item and fluid pipes, and the pump and conveyor covers
            server.runCommand("tp @p 21.5 -59 8 0 35");
            context.waitTicks(20);
            context.takeScreenshot("gtceu_logistics_item_pipe");
            context.runOnClient(mc -> mc.levelExtractor.allChanged());
            context.waitTicks(20);
            context.takeScreenshot("gtceu_logistics_item_pipe_rerendered");
            server.runCommand("tp @p 21.5 -59 12 0 35");
            context.waitTicks(20);
            context.takeScreenshot("gtceu_logistics_fluid_pipe");
            server.runCommand("tp @p 18 -59.5 14.5 -90 25");
            context.waitTicks(20);
            context.takeScreenshot("gtceu_logistics_cover");
            // the same machine's side without a cover, for comparison (the other tank's east face)
            server.runCommand("tp @p 24 -59.5 14.5 90 25");
            context.waitTicks(20);
            context.takeScreenshot("gtceu_logistics_no_cover");

            List<String> problems = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                List<String> p = new ArrayList<>();
                int piped = count(level, PIPE_CHEST);
                if (piped == 0) p.add("item pipe moved nothing from the hopper to the chest");
                FluidStack pumped = fluids(level, TANK_B).getFluidInTank(0);
                if (pumped.isEmpty()) p.add("pump cover moved no water through the fluid pipe");
                int conveyed = count(level, CONVEYOR_CHEST);
                if (conveyed == 0) p.add("conveyor cover moved nothing out of the super chest");
                var macerator = (SimpleSteamMachine) MetaMachine.getMachine(level, STEAM_MACERATOR);
                ItemStack crushed = macerator.exportItems.getStackInSlot(0);
                if (crushed.isEmpty()) {
                    p.add("steam macerator made nothing (steam " + macerator.steamTank.getFluidInTank(0) + ")");
                }
                var oven = (PrimitiveWorkableMachine) MetaMachine.getMachine(level, COKE_OVEN);
                ItemStack charcoal = oven.exportItems.getStackInSlot(0);
                if (charcoal.isEmpty()) p.add("coke oven made no charcoal (formed " + oven.isFormed() + ")");
                com.gregtechceu.gtceu.GTCEu.LOGGER.info("[Logistics] piped {}, pumped {}, conveyed {}, crushed {}, " +
                        "charcoal {}, creosote {}", piped, pumped, conveyed, crushed, charcoal,
                        oven.exportFluids.getFluidInTank(0));
                return p;
            });
            if (!problems.isEmpty()) throw new AssertionError(String.join("; ", problems));
        }
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
    }

    private static void setBlock(ServerLevel level, BlockPos pos, String id) {
        var server = level.getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "setblock %d %d %d %s".formatted(pos.getX(), pos.getY(), pos.getZ(), id));
    }

    private static PipeBlockEntity<?, ?> pipe(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> pipe) return pipe;
        throw new AssertionError("No pipe at " + pos);
    }

    private static IFluidHandler fluids(ServerLevel level, BlockPos pos) {
        var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (handler == null) throw new AssertionError("No fluid handler at " + pos);
        return handler;
    }

    private static void cover(ServerLevel level, BlockPos pos, Direction side, String item,
                              com.gregtechceu.gtceu.api.cover.CoverDefinition definition) {
        var machine = MetaMachine.getMachine(level, pos);
        var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));
        if (!machine.getCoverContainer().placeCoverOnSide(side, stack, definition, null)) {
            throw new AssertionError("Could not place " + item + " on " + pos);
        }
    }

    private static int count(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof Container container)) return 0;
        int n = 0;
        for (int i = 0; i < container.getContainerSize(); i++) n += container.getItem(i).getCount();
        return n;
    }
}
