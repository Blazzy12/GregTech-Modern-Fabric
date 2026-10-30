package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;

/**
 * GregTech's machinery working together: a creative energy source powers a macerator over a cable, the
 * macerator auto-outputs into a chest, and everything survives saving and reloading the world.
 */
public class GTMechanicsGameTest implements FabricClientGameTest {

    private static final BlockPos SOURCE = new BlockPos(4, -60, 6);
    private static final BlockPos CABLE = SOURCE.east();
    private static final BlockPos MACERATOR = CABLE.east();
    private static final BlockPos CHEST = MACERATOR.south();
    private static final BlockPos HOPPER = MACERATOR.above();

    @Override
    public void runTest(ClientGameTestContext context) {
        if (GTRemoteServerGameTest.ADDRESS != null) return;
        TestWorldSave save;
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            save = singleplayer.getWorldSave();
            TestServerContext server = singleplayer.getServer();
            singleplayer.getConnection().waitForChunksRender();

            place(server, SOURCE, "gtceu:creative_energy");
            place(server, CABLE, "gtceu:tin_single_cable");
            place(server, MACERATOR, "gtceu:lv_macerator[facing=north]");
            place(server, CHEST, "minecraft:chest");
            // a vanilla hopper feeds the macerator through Fabric's transfer API
            place(server, HOPPER, "minecraft:hopper[facing=down]");
            context.waitTicks(10);

            server.runOnServer(s -> {
                var source = MetaMachine.getMachine(s.overworld(), SOURCE);
                if (source == null) throw new AssertionError("No creative energy source");
                setField(source, "voltage", 32L);
                setField(source, "amps", 1);
                setField(source, "workingEnabled", true);

                // pipes connect when a player places them; /setblock does not, so connect it like a wrench would
                if (!(s.overworld().getBlockEntity(CABLE) instanceof
                        com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity<?, ?> cable)) {
                    throw new AssertionError("No cable at " + CABLE);
                }
                cable.setConnection(Direction.WEST, true, false);
                cable.setConnection(Direction.EAST, true, false);

                var macerator = macerator(s);
                var output = macerator.getTrait(AutoOutputTrait.class);
                if (output == null) throw new AssertionError("Macerator has no auto output");
                output.setItemOutputDirection(Direction.SOUTH);
                output.setAllowAutoOutputItems(true);
                macerator.importItems.insertItem(0, new ItemStack(Items.IRON_ORE, 4), false);
                if (s.overworld().getBlockEntity(HOPPER) instanceof Container hopper) {
                    hopper.setItem(0, new ItemStack(Items.IRON_ORE, 2));
                }
            });
            context.waitTicks(1200);

            String state = server.computeOnServer(s -> describe(s));
            boolean hopperEmpty = server.computeOnServer(s ->
                    s.overworld().getBlockEntity(HOPPER) instanceof Container hopper && hopper.isEmpty());
            if (!hopperEmpty) throw new AssertionError("The hopper did not feed the macerator: " + state);
            if (!state.contains("chest=") || !state.contains("crushed_iron_ore")) {
                throw new AssertionError("Powered over a cable, the macerator should fill the chest: " + state);
            }

            // crafting recipes and GT recipes are loaded
            int crafting = server.computeOnServer(s -> (int) s.getRecipeManager().getRecipes().stream()
                    .filter(h -> h.value().getType() == RecipeType.CRAFTING &&
                            h.id().identifier().getNamespace().equals("gtceu"))
                    .count());
            if (crafting < 100) throw new AssertionError("Only " + crafting + " GT crafting recipes");

            testFluids(context, server);
        }

        // everything is still there after saving and loading the world
        try (TestSingleplayerContext singleplayer = save.open()) {
            singleplayer.getConnection().waitForChunksRender();
            context.waitTicks(20);
            String state = singleplayer.getServer().computeOnServer(GTMechanicsGameTest::describe);
            if (!state.contains("chest=") || !state.contains("crushed_iron_ore") || !state.contains("energy=")) {
                throw new AssertionError("State lost on reload: " + state);
            }
            if (singleplayer.getServer().computeOnServer(s -> macerator(s).energyContainer.getEnergyStored()) <= 0) {
                throw new AssertionError("Macerator energy lost on reload: " + state);
            }
            context.takeScreenshot("gtceu_reloaded");
        }
    }

    private static final BlockPos TANK = new BlockPos(-4, -60, 6);
    private static final BlockPos BOILER = TANK.west(2);

    /** Fluids move between GT and Fabric's transfer API; a solid boiler turns water and coal into steam. */
    private static void testFluids(ClientGameTestContext context, TestServerContext server) {
        place(server, TANK, "gtceu:lv_super_tank");
        place(server, BOILER, "gtceu:lp_steam_solid_boiler");
        context.waitTicks(5);
        String tank = server.computeOnServer(s -> {
            var level = s.overworld();
            var storage = net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.find(level, TANK, Direction.UP);
            if (storage == null) return "no fluid storage";
            var water = net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant.of(
                    net.minecraft.world.level.material.Fluids.WATER);
            long bucket = net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
            try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
                long in = storage.insert(water, 2 * bucket, tx);
                tx.commit();
                if (in != 2 * bucket) return "inserted " + in;
            }
            try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
                long out = storage.extract(water, bucket, tx);
                tx.abort();
                if (out != bucket) return "extracted " + out;
            }
            var handler = com.gregtechceu.gtceu.api.capability.GTCapabilityHelper.getFluidHandler(level, TANK,
                    Direction.UP);
            return handler == null ? "no GT handler" : handler.getFluidInTank(0).getAmount() + "";
        });
        if (!tank.equals("2000")) throw new AssertionError("Tank via Fabric transfer: " + tank);

        server.runOnServer(s -> {
            var boiler = (com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine) MetaMachine
                    .getMachine(s.overworld(), BOILER);
            boiler.waterTank.fill(new com.gregtechceu.gtceu.compat.fluids.FluidStack(
                    net.minecraft.world.level.material.Fluids.WATER, 16000),
                    com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            boiler.fuelHandler.insertItem(0, new ItemStack(Items.COAL, 8), false);
        });
        // a low-pressure boiler heats 1 degree per 24 ticks and boils from 100, so check it heats up, then preheat it
        context.waitTicks(240);
        int heated = server.computeOnServer(s -> ((com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine)
                MetaMachine.getMachine(s.overworld(), BOILER)).getCurrentTemperature());
        if (heated <= 0) throw new AssertionError("Boiler did not heat up");
        server.runOnServer(s -> setField(MetaMachine.getMachine(s.overworld(), BOILER), "currentTemperature", 200));
        context.waitTicks(100);
        String steam = server.computeOnServer(s -> {
            var boiler = (com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine) MetaMachine
                    .getMachine(s.overworld(), BOILER);
            return boiler.steamTank.getFluidInTank(0) + " water " + boiler.waterTank.getFluidInTank(0) +
                    " fuel " + boiler.fuelHandler.getStackInSlot(0);
        });
        if (!steam.contains("steam")) throw new AssertionError("Boiler made no steam: " + steam);
    }

    private static void place(TestServerContext server, BlockPos pos, String block) {
        server.runCommand("setblock %d %d %d %s".formatted(pos.getX(), pos.getY(), pos.getZ(), block));
    }

    private static WorkableTieredMachine macerator(MinecraftServer server) {
        var machine = MetaMachine.getMachine(server.overworld(), MACERATOR);
        if (!(machine instanceof WorkableTieredMachine workable)) {
            throw new AssertionError("No macerator, found " + machine);
        }
        return workable;
    }

    private static String describe(MinecraftServer server) {
        var macerator = macerator(server);
        StringBuilder sb = new StringBuilder("energy=").append(macerator.energyContainer.getEnergyStored());
        var level = server.overworld();
        sb.append(" cableCap=").append(com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
                .getEnergyContainer(level, CABLE, Direction.WEST));
        sb.append(" maceratorCap=").append(com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
                .getEnergyContainer(level, MACERATOR, Direction.WEST));
        sb.append(" cableBE=").append(level.getBlockEntity(CABLE));
        if (level.getBlockEntity(CABLE) instanceof com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity<?, ?> pipe) {
            sb.append(" connections=").append(Integer.toBinaryString(pipe.getConnections()));
        }
        sb.append(" input=").append(macerator.importItems.getStackInSlot(0));
        if (server.overworld().getBlockEntity(CHEST) instanceof Container chest) {
            sb.append(" chest=");
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (!chest.getItem(i).isEmpty()) sb.append(chest.getItem(i)).append(',');
            }
        }
        return sb.toString();
    }

    private static void setField(Object target, String name, Object value) {
        try {
            for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
                try {
                    var field = c.getDeclaredField(name);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException ignored) {}
            }
            throw new AssertionError("No field " + name + " on " + target);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
