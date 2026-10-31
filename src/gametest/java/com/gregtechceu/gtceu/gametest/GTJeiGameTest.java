package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.GTCEu;

import brachy.modularui.integration.jei.ModularUIJeiPlugin;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import java.util.ArrayList;
import java.util.List;

/**
 * GT fluids in JEI: every source fluid has a real name, a sprite and a visible colour, and gases show up in the
 * ingredient list and their recipes.
 */
public class GTJeiGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!GTTests.enabled("jei")) return;
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.waitFor(mc -> ModularUIJeiPlugin.getRuntime() != null, 20 * 60);

            List<String> problems = context.computeOnClient(GTJeiGameTest::checkFluids);
            problems.addAll(context.computeOnClient(GTJeiGameTest::checkItemNames));
            problems.forEach(p -> GTCEu.LOGGER.warn("[JEI fluid check] {}", p));

            context.setScreen(() -> new InventoryScreen(context.computeOnClient(mc -> mc.player)));
            context.waitTicks(10);
            for (String filter : new String[] { "hydrogen", "steam", "@gtceu oxygen" }) {
                context.runOnClient(mc -> ModularUIJeiPlugin.getRuntime().getIngredientFilter().setFilterText(filter));
                context.waitTicks(10);
                context.takeScreenshot("gtceu_jei_fluids_" + filter.replaceAll("[^a-z]", "_"));
            }
            context.runOnClient(mc -> ModularUIJeiPlugin.getRuntime().getIngredientFilter().setFilterText(""));

            // recipes that make hydrogen
            context.runOnClient(mc -> {
                var runtime = ModularUIJeiPlugin.getRuntime();
                var fluidHelper = runtime.getJeiHelpers().getPlatformFluidHelper();
                var hydrogen = BuiltInRegistries.FLUID.get(GTCEu.id("hydrogen")).orElseThrow();
                Object ingredient = fluidHelper.create(hydrogen, fluidHelper.bucketVolume());
                showOutput(runtime, fluidHelper.getFluidIngredientType(), ingredient);
            });
            context.waitTicks(20);
            context.takeScreenshot("gtceu_jei_hydrogen_recipes");
            // hover the first fluid output for its tooltip (GT's fluid info is added to JEI's)
            context.getInput().setCursorPos(470, 280);
            context.waitTicks(10);
            context.takeScreenshot("gtceu_jei_fluid_tooltip");
            int emptySlots = context.computeOnClient(GTJeiGameTest::checkCokeOvenSlots);
            if (emptySlots > 0) problems.add(emptySlots + " coke oven recipe slots are empty in JEI");
            showFluidOutput(context, "creosote");
            context.takeScreenshot("gtceu_jei_creosote_recipes");
            context.setScreen(() -> null);

            // a super tank's ModularUI draws its fluid itself
            world.getServer().runCommand("execute as @p at @s run setblock ~ ~ ~2 gtceu:lv_super_tank");
            world.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 30");
            context.waitTicks(10);
            world.getServer().runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var tank = com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(s.overworld(),
                        player.blockPosition().south(2));
                var handler = tank.getLevel().getCapability(
                        com.gregtechceu.gtceu.compat.capabilities.Capabilities.FluidHandler.BLOCK,
                        tank.getBlockPos(), net.minecraft.core.Direction.UP);
                handler.fill(new com.gregtechceu.gtceu.compat.fluids.FluidStack(
                        BuiltInRegistries.FLUID.get(GTCEu.id("hydrogen")).orElseThrow().value(), 8000),
                        com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            });
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyUse);
            context.waitTicks(20);
            context.takeScreenshot("gtceu_tank_ui_hydrogen");
            context.setScreen(() -> null);

            if (!problems.isEmpty()) {
                throw new AssertionError(problems.size() + " GT fluids render or read wrong in JEI, first: " +
                        problems.getFirst());
            }
        }
    }

    /** Every GT item in JEI has a translated name: no raw keys or unfilled placeholders. */
    private static List<String> checkItemNames(Minecraft mc) {
        var runtime = ModularUIJeiPlugin.getRuntime();
        var helper = runtime.getIngredientManager().getIngredientHelper(mezz.jei.api.constants.VanillaTypes.ITEM_STACK);
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (var stack : runtime.getIngredientManager().getAllIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK)) {
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!id.getNamespace().equals(GTCEu.MOD_ID)) continue;
            checked++;
            String name = helper.getDisplayName(stack);
            if (name.isBlank() || name.contains("%s") || name.matches(".*\\b(item|block|gtceu|material|tagprefix)\\.[a-z_]+.*")) {
                problems.add("bad item name: " + id + " '" + name + "'");
            }
        }
        GTCEu.LOGGER.info("[JEI item check] checked {} GT items, {} problems", checked, problems.size());
        problems.stream().limit(30).forEach(p -> GTCEu.LOGGER.warn("[JEI item check] {}", p));
        return problems;
    }

    /** Logs what each slot of a coke oven recipe (items in, charcoal and creosote out) holds, as JEI sees it. */
    private static int checkCokeOvenSlots(Minecraft mc) {
        var runtime = ModularUIJeiPlugin.getRuntime();
        var type = com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory.TYPES
                .apply(com.gregtechceu.gtceu.common.data.GTRecipeTypes.COKE_OVEN_RECIPES.getCategory());
        return checkSlots(runtime, type);
    }

    /** @return how many of the recipe's slots JEI shows nothing in */
    private static <R> int checkSlots(mezz.jei.api.runtime.IJeiRuntime runtime,
                                     mezz.jei.api.recipe.types.IRecipeType<R> type) {
        var manager = runtime.getRecipeManager();
        R recipe = manager.createRecipeLookup(type).get().findFirst().orElseThrow();
        var layout = manager.createRecipeLayoutDrawable(manager.getRecipeCategory(type), recipe,
                runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElseThrow();
        int empty = 0;
        for (var slot : layout.getRecipeSlotsView().getSlotViews()) {
            GTCEu.LOGGER.info("[JEI slot] {} displayed={} all={}", slot.getRole(),
                    slot.getDisplayedIngredient().map(i -> i.getType().getUid() + ":" + i.getIngredient()).orElse("none"),
                    slot.getAllIngredientsList().size());
            if (slot.getDisplayedIngredient().isEmpty()) empty++;
        }
        return empty;
    }

    private static void showFluidOutput(ClientGameTestContext context, String fluidId) {
        context.runOnClient(mc -> {
            var runtime = ModularUIJeiPlugin.getRuntime();
            var fluidHelper = runtime.getJeiHelpers().getPlatformFluidHelper();
            var fluid = BuiltInRegistries.FLUID.get(GTCEu.id(fluidId)).orElseThrow();
            showOutput(runtime, fluidHelper.getFluidIngredientType(), fluidHelper.create(fluid, fluidHelper.bucketVolume()));
        });
        context.waitTicks(20);
    }

    @SuppressWarnings("unchecked")
    private static <T> void showOutput(mezz.jei.api.runtime.IJeiRuntime runtime,
                                       mezz.jei.api.ingredients.IIngredientType<T> type, Object ingredient) {
        runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, type, (T) ingredient));
    }

    private static List<String> checkFluids(Minecraft mc) {
        var runtime = ModularUIJeiPlugin.getRuntime();
        @SuppressWarnings("unchecked")
        var fluids = (mezz.jei.api.helpers.IPlatformFluidHelper<IJeiFluidIngredient>) runtime.getJeiHelpers()
                .getPlatformFluidHelper();
        var type = fluids.getFluidIngredientType();
        var helper = runtime.getIngredientManager().getIngredientHelper(type);
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (IJeiFluidIngredient ingredient : runtime.getIngredientManager().getAllIngredients(type)) {
            Fluid fluid = ingredient.getFluidVariant().getFluid();
            var id = BuiltInRegistries.FLUID.getKey(fluid);
            if (!id.getNamespace().equals(GTCEu.MOD_ID)) continue;
            checked++;
            String name = helper.getDisplayName(ingredient);
            int color = FluidVariantRendering.getColor(ingredient.getFluidVariant());
            var sprite = mc.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState())
                    .stillMaterial().sprite();
            String desc = id + " name='" + name + "' color=" + Integer.toHexString(color) + " sprite=" +
                    sprite.contents().name();
            if (name.isBlank() || (name.equals("Air") && !id.getPath().equals("air")) || name.contains("gtceu.") || name.contains("block.") ||
                    name.contains("fluid.")) {
                problems.add("bad name: " + desc);
            } else if ((color >>> 24) == 0) {
                problems.add("transparent colour: " + desc);
            } else if (sprite.contents().name().getPath().contains("missingno")) {
                problems.add("missing sprite: " + desc);
            }
            if (checked <= 3 || id.getPath().equals("hydrogen") || id.getPath().equals("steam") ||
                    (id.getPath().equals("potion") && name.contains("Night"))) {
                GTCEu.LOGGER.info("[JEI fluid check] {}", desc);
            }
        }
        GTCEu.LOGGER.info("[JEI fluid check] checked {} GT fluids, {} problems", checked, problems.size());
        if (checked == 0) problems.add("JEI lists no GT fluids");
        return problems;
    }
}
