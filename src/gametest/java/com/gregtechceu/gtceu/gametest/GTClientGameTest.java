package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import brachy.modularui.integration.jei.ModularUIJeiPlugin;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Plays through GregTech in a flat test world and takes screenshots of the world, an inventory of GT items and a
 * machine GUI, for checking the port by eye ({@code build/run/clientGameTest/screenshots}).
 */
public class GTClientGameTest implements FabricClientGameTest {

    private static final String[] BLOCKS = {
            "gtceu:lv_macerator", "gtceu:lv_electric_furnace", "gtceu:lp_steam_solid_boiler",
            "gtceu:lv_machine_hull", "gtceu:white_lamp", "gtceu:lv_super_chest", "gtceu:coke_oven",
            "gtceu:aluminium_block", "gtceu:aluminium_frame", "gtceu:copper_single_cable",
            "gtceu:bronze_normal_fluid_pipe", "gtceu:steam_machine_casing",
    };

    private static final String[] ITEMS = {
            "gtceu:lv_macerator", "gtceu:white_lamp", "gtceu:aluminium_ingot", "gtceu:copper_dust",
            "gtceu:fluid_cell", "gtceu:steel_hammer", "gtceu:basic_electronic_circuit", "gtceu:creosote_bucket",
            "gtceu:copper_single_cable",
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        if (GTRemoteServerGameTest.ADDRESS != null) return;
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            var server = singleplayer.getServer();
            singleplayer.getConnection().waitForChunksRender();
            server.runCommand("time set noon");
            // face south and put a row of blocks in front of the player
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 15");
            for (int i = 0; i < BLOCKS.length; i++) {
                server.runCommand("execute as @p at @s run setblock ~" + (i - BLOCKS.length / 2) + " ~ ~4 " + BLOCKS[i]);
            }
            context.waitTicks(40);
            singleplayer.getConnection().waitForChunksRender();
            context.takeScreenshot("gtceu_world");

            for (int i = 0; i < ITEMS.length; i++) {
                server.runCommand("item replace entity @p hotbar." + i + " with " + ITEMS[i]);
            }
            context.waitTicks(5);
            context.setScreen(() -> new InventoryScreen(context.computeOnClient(mc -> mc.player)));
            context.waitTicks(5);
            context.takeScreenshot("gtceu_inventory");
            context.setScreen(() -> null);

            // put a macerator right in front of the player, look at it and open its UI
            server.runCommand("execute as @p at @s run setblock ~ ~ ~2 gtceu:lv_macerator");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 30");
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyUse);
            context.waitTicks(20);
            context.takeScreenshot("gtceu_machine_ui");
            context.setScreen(() -> null);
            // let the server's close-GUI reply arrive before opening other screens
            context.waitTicks(20);

            // creative tabs must not list the same stack twice
            String duplicates = context.computeOnClient(mc -> {
                StringBuilder sb = new StringBuilder();
                var params = new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                        mc.level.enabledFeatures(), true, mc.level.registryAccess());
                for (String tab : java.util.List.of("decoration", "item", "machine", "tool", "material_item",
                        "material_block", "material_pipe", "material_fluid")) {
                    var seen = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();
                    new com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.RegistrateDisplayItemsGenerator(tab,
                            com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE).accept(params,
                                    (stack, visibility) -> {
                                        if (!seen.add(stack)) sb.append(tab).append(':').append(stack).append(' ');
                                    });
                }
                return sb.toString();
            });
            if (!duplicates.isEmpty()) throw new AssertionError("Duplicate creative tab stacks: " + duplicates);
            testJei(context);
            testMacerator(context, server);
            // look at the macerator, which is working, for Jade's overlay
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 30");
            context.waitTicks(20);
            context.takeScreenshot("gtceu_jade");
            testCokeOven(context, server);
            testMaps(context);
            context.waitTicks(10);
            context.takeScreenshot("gtceu_multiblock");
        }
    }

    /** JEI lists GT's items and shows the macerator's recipes. */
    private static void testJei(ClientGameTestContext context) {
        context.waitFor(mc -> ModularUIJeiPlugin.getRuntime() != null, 20 * 60);
        context.setScreen(() -> new InventoryScreen(context.computeOnClient(mc -> mc.player)));
        context.waitTicks(20);
        context.takeScreenshot("gtceu_jei_inventory");
        context.runOnClient(mc -> {
            var runtime = ModularUIJeiPlugin.getRuntime();
            var stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getValue(net.minecraft.resources.Identifier.parse("gtceu:lv_macerator")));
            runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(
                    RecipeIngredientRole.CRAFTING_STATION, VanillaTypes.ITEM_STACK, stack));
        });
        context.waitTicks(20);
        context.takeScreenshot("gtceu_jei_macerator");
        context.setScreen(() -> null);
    }

    /** A powered LV macerator turns iron ore into crushed iron ore. */
    private static void testMacerator(ClientGameTestContext context, TestServerContext server) {
        BlockPos pos = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().blockPosition()
                .south(2));
        server.runOnServer(s -> {
            var machine = (WorkableTieredMachine) MetaMachine.getMachine(s.overworld(), pos);
            if (machine == null) throw new AssertionError("No macerator at " + pos);
            machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
            machine.importItems.insertItem(0, new ItemStack(Items.IRON_ORE, 4), false);
        });
        context.waitTicks(900);
        String out = server.computeOnServer(s -> {
            var machine = (WorkableTieredMachine) MetaMachine.getMachine(s.overworld(), pos);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < machine.exportItems.getSlots(); i++) {
                ItemStack stack = machine.exportItems.getStackInSlot(i);
                if (!stack.isEmpty()) sb.append(stack.getCount()).append('x').append(stack.getItem()).append(' ');
            }
            sb.append("| energy ").append(machine.energyContainer.getEnergyStored())
                    .append(" in ").append(machine.importItems.getStackInSlot(0))
                    .append(" working ").append(machine.recipeLogic.isWorking())
                    .append(" idle ").append(machine.recipeLogic.isIdle())
                    .append(" suspend ").append(machine.recipeLogic.isSuspend())
                    .append(" enabled ").append(machine.recipeLogic.isWorkingEnabled())
                    .append(" lookup ").append(machine.getRecipeType().searchRecipe(machine, r -> true).hasNext())
                    .append(" recipes ").append(s.getRecipeManager().getRecipes().stream()
                            .filter(h -> h.value().getType() == machine.getRecipeType()).count());
            ItemStack ore = new ItemStack(Items.IRON_ORE);
            long matching = s.getRecipeManager().getRecipes().stream()
                    .filter(h -> h.value() instanceof com.gregtechceu.gtceu.api.recipe.GTRecipe r &&
                            r.recipeType == machine.getRecipeType() &&
                            r.getInputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP)
                                    .stream().anyMatch(c -> c.content() instanceof
                                            com.gregtechceu.gtceu.compat.common.crafting.SizedIngredient si &&
                                            si.ingredient().test(ore)))
                    .count();
            sb.append(" matching ").append(matching)
                    .append(" oreTag ").append(ore.is(net.minecraft.tags.TagKey.create(
                            net.minecraft.core.registries.Registries.ITEM,
                            net.minecraft.resources.Identifier.parse("c:ores/iron"))));
            machine.importItems.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
            sb.append(" cobble ").append(machine.getRecipeType().searchRecipe(machine, r -> true).hasNext())
                    .append(" proxies ").append(machine.hasCapabilityProxies())
                    .append(" dbOre ").append(machine.getRecipeType().db().find(java.util.Map.of(
                            com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP,
                            java.util.List.<Object>of(ore)), r -> true) != null);
            return sb.toString();
        });
        if (!out.contains("crushed_iron_ore")) {
            throw new AssertionError("Macerator output after 900 ticks: '" + out + "'");
        }

        // CC: Tweaked sees the machine as a peripheral with GT's generic methods
        String types = server.computeOnServer(s -> {
            var level = s.overworld();
            var peripheral = dan200.computercraft.impl.Peripherals.getGenericPeripheral(level, pos,
                    net.minecraft.core.Direction.NORTH, level.getBlockEntity(pos));
            if (peripheral == null) return "none";
            // the generic peripheral's methods, as CC builds them for a computer
            var methods = new java.util.TreeSet<String>();
            if (peripheral instanceof dan200.computercraft.shared.peripheral.generic.GenericPeripheral generic) {
                methods.addAll(java.util.Arrays.asList(generic.getMethodNames()));
            }
            return peripheral.getType() + " " + methods;
        });
        if (!types.contains("getProgress") || !types.contains("getEnergyStored")) {
            throw new AssertionError("CC peripheral types of the macerator: " + types);
        }
    }

    /** GT's ore vein markers reach the installed map mods; the world map shows them. */
    private static void testMaps(ClientGameTestContext context) {
        boolean added = context.computeOnClient(mc -> {
            var veins = mc.level.registryAccess()
                    .lookupOrThrow(com.gregtechceu.gtceu.api.registry.GTRegistries.Keys.ORE_VEIN);
            var definition = veins.listElements().findFirst().orElseThrow();
            BlockPos center = mc.player.blockPosition().offset(24, 0, 0);
            var vein = new com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata(
                    net.minecraft.world.level.ChunkPos.containing(center), center, definition);
            return com.gregtechceu.gtceu.integration.map.GroupingMapRenderer.getInstance().addMarker(
                    net.minecraft.network.chat.Component.literal("Test vein"), mc.level.dimension(), vein,
                    "gtceu_test_vein");
        });
        if (!added) throw new AssertionError("No map integration took the ore vein marker");
        context.runOnClient(mc -> {
            if (!com.gregtechceu.gtceu.integration.map.ButtonState.isEnabled("ore_veins")) {
                com.gregtechceu.gtceu.integration.map.ButtonState.toggleButton("ore_veins");
            }
        });
        context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_M);
        context.waitTicks(40);
        context.takeScreenshot("gtceu_world_map");
        context.setScreen(() -> null);
        context.waitTicks(5);
    }

    /** A coke oven built from bricks forms. */
    private static void testCokeOven(ClientGameTestContext context, TestServerContext server) {
        // 3x3x3 of bricks east of the player, hollow, controller in the middle of the north face
        server.runCommand("execute as @p at @s run fill ~4 ~ ~4 ~6 ~2 ~6 gtceu:coke_oven_bricks");
        server.runCommand("execute as @p at @s run setblock ~5 ~1 ~5 air");
        server.runCommand("execute as @p at @s run setblock ~5 ~1 ~4 gtceu:coke_oven[facing=north]");
        BlockPos controller = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().blockPosition()
                .offset(5, 1, 4));
        context.waitTicks(100);
        boolean formed = server.computeOnServer(s -> MetaMachine.getMachine(s.overworld(), controller)
                instanceof MultiblockControllerMachine controllerMachine && controllerMachine.isFormed());
        if (!formed) throw new AssertionError("Coke oven at " + controller + " did not form");
        server.runCommand("execute as @p at @s run tp @s ~ ~ ~ -30 10");
    }
}
