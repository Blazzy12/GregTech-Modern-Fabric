package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;

import brachy.modularui.integration.jei.ModularUIJeiPlugin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;

/**
 * Plays GregTech against a real dedicated server: machine state syncs to the client, machine UIs open over the
 * network and JEI receives GT's recipes.
 */
public class GTDedicatedServerGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        if (GTRemoteServerGameTest.ADDRESS != null) return;
        try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
            try (var connection = server.connect()) {
                connection.waitForChunksRender();
                server.runCommand("time set noon");
                server.runCommand("gamemode creative @a");
                server.runCommand("execute as @p at @s run setblock ~ ~ ~2 gtceu:lv_macerator");
                server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 30");
                BlockPos pos = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().blockPosition()
                        .south(2));

                // run a recipe on the server and check the client sees the machine working
                server.runOnServer(s -> {
                    var machine = (WorkableTieredMachine) MetaMachine.getMachine(s.overworld(), pos);
                    if (machine == null) throw new AssertionError("No macerator at " + pos);
                    machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
                    machine.importItems.insertItem(0, new ItemStack(Items.IRON_ORE, 4), false);
                });
                context.waitTicks(40);
                boolean clientActive = context.computeOnClient(mc -> MetaMachine.getMachine(mc.level, pos)
                        instanceof WorkableTieredMachine m && m.recipeLogic.isActive());
                if (!clientActive) throw new AssertionError("Client does not see the macerator working");
                context.takeScreenshot("gtceu_server_world");

                // the machine UI opens through the server
                context.getInput().pressKey(options -> options.keyUse);
                context.waitTicks(20);
                String screen = context.computeOnClient(mc -> mc.gui.screen() == null ? "none" :
                        mc.gui.screen().getClass().getName());
                if (!screen.contains("modularui")) throw new AssertionError("Machine UI did not open: " + screen);
                context.takeScreenshot("gtceu_server_machine_ui");
                context.setScreen(() -> null);
                context.waitTicks(20);

                // JEI gets GT's recipes from the server
                context.waitFor(mc -> ModularUIJeiPlugin.getRuntime() != null, 20 * 60);
                long recipes = context.computeOnClient(mc -> ModularUIJeiPlugin.getRuntime().getRecipeManager()
                        .createRecipeLookup(GTRecipeJEICategory.TYPES
                                .apply(GTRecipeTypes.MACERATOR_RECIPES.getCategory()))
                        .get().count());
                if (recipes == 0) throw new AssertionError("JEI has no macerator recipes from the server");
            }
        }
    }
}
