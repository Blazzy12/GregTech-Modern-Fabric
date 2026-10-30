package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * Joins a dedicated server running in its own process, where Fabric strips client-only code, and opens a machine UI
 * there. Only runs when GTCEU_REMOTE_SERVER names the server (e.g. localhost:25565); the other tests are skipped then.
 * The server must be offline-mode, and something has to place an LV macerator two blocks south of the player once
 * they join (see research/tools/remotetest.sh).
 */
public class GTRemoteServerGameTest implements FabricClientGameTest {

    static final String ADDRESS = System.getenv("GTCEU_REMOTE_SERVER");

    @Override
    public void runTest(ClientGameTestContext context) {
        if (ADDRESS == null) return;
        context.runOnClient(mc -> ConnectScreen.startConnecting(new TitleScreen(), mc, ServerAddress.parseString(ADDRESS),
                new ServerData("GT test", ADDRESS, ServerData.Type.OTHER), false, null));
        context.waitFor(mc -> mc.level != null && mc.player != null, 20 * 120);

        // the server script places the machine next to us
        context.waitFor(mc -> mc.level.getBlockState(mc.player.blockPosition().south(2))
                .getBlock() instanceof MetaMachineBlock, 20 * 60);
        context.waitTicks(60);
        context.takeScreenshot("gtceu_remote_world");

        context.getInput().pressKey(options -> options.keyUse);
        context.waitTicks(40);
        String screen = context.computeOnClient(mc -> mc.gui.screen() == null ? "none" :
                mc.gui.screen().getClass().getName());
        context.takeScreenshot("gtceu_remote_machine_ui");
        if (!screen.contains("modularui")) throw new AssertionError("Machine UI did not open: " + screen);
        context.setScreen(() -> null);
        context.waitTicks(20);
        BlockPos pos = context.computeOnClient(mc -> mc.player.blockPosition());
        context.runOnClient(mc -> mc.disconnect(new TitleScreen(), false));
        context.waitFor(mc -> mc.level == null, 20 * 30);
        System.out.println("GTCEU remote test passed at " + pos);
    }
}
