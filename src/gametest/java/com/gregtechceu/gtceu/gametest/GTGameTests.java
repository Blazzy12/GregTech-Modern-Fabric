package com.gregtechceu.gtceu.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;

/**
 * The single client game test entrypoint. It runs each test whose mods are present, and only then loads its class:
 * the JEI tests can't even load without JEI ({@code -PwithRei=true} runs REI instead of JEI), nor the REI one
 * without REI.
 */
public class GTGameTests implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        boolean jei = FabricLoader.getInstance().isModLoaded("jei");
        boolean rei = FabricLoader.getInstance().isModLoaded("roughlyenoughitems");
        if (jei) new GTClientGameTest().runTest(context);
        new GTMechanicsGameTest().runTest(context);
        new GTWorldGameTest().runTest(context);
        new GTLogisticsGameTest().runTest(context);
        new GTToolsGameTest().runTest(context);
        if (jei) new GTJeiGameTest().runTest(context);
        if (rei) new GTReiGameTest().runTest(context);
        if (jei) new GTDedicatedServerGameTest().runTest(context);
        new GTRemoteServerGameTest().runTest(context);
    }
}
