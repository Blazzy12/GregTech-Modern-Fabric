package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.List;

/**
 * GT in REI (run with {@code -PwithRei=true}, which swaps JEI for REI): GT's categories have displays, their recipes
 * list their ingredients for lookups, and recipe views with item and fluid slots open and draw.
 */
public class GTReiGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!GTTests.enabled("rei") || !FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) return;
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.waitFor(mc -> gtDisplays() > 1000, 20 * 120);

            String problems = context.computeOnClient(mc -> {
                StringBuilder sb = new StringBuilder();
                GTCEu.LOGGER.info("[REI check] {} GT displays", gtDisplays());
                for (String category : List.of("macerator", "coke_oven", "electric_blast_furnace", "multiblock_info",
                        "ore_processing_diagram", "ore_vein_diagram", "bedrock_fluid_diagram", "programmed_circuit")) {
                    List<Display> displays = DisplayRegistry.getInstance().get(CategoryIdentifier.of(GTCEu.id(category)));
                    GTCEu.LOGGER.info("[REI check] {}: {} displays", category, displays.size());
                    if (displays.isEmpty()) sb.append(category).append(" has no displays; ");
                }
                var hydrogen = BuiltInRegistries.FLUID.getValue(GTCEu.id("hydrogen"));
                var sprite = dev.architectury.hooks.client.fluid.ClientFluidStackHooks.getStillTexture(
                        dev.architectury.fluid.FluidStack.create(hydrogen, 81000));
                GTCEu.LOGGER.info("[REI check] hydrogen sprite {}", sprite == null ? null : sprite.contents().name());
                if (sprite == null) sb.append("no hydrogen sprite; ");
                long gtFluids = me.shedaniel.rei.api.client.registry.entry.EntryRegistry.getInstance().getEntryStacks()
                        .filter(e -> e.getType() == me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes.FLUID)
                        .filter(e -> BuiltInRegistries.FLUID.getKey(e.<dev.architectury.fluid.FluidStack>castValue()
                                .getFluid()).getNamespace().equals(GTCEu.MOD_ID))
                        .count();
                GTCEu.LOGGER.info("[REI check] {} GT fluids in REI's list", gtFluids);
                if (gtFluids == 0) sb.append("no GT fluids in REI's list; ");
                var converted = brachy.modularui.integration.rei.REIStackConverter.convertToReiEntry(
                        brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList.of(
                                new com.gregtechceu.gtceu.compat.fluids.FluidStack(hydrogen, 1000)),
                        1f, java.util.function.UnaryOperator.identity());
                GTCEu.LOGGER.info("[REI check] converted fluid slot: {}", converted.stream()
                        .map(e -> e.getType().getId() + " " + e.asFormattedText().getString()).toList());
                if (converted.isEmpty()) sb.append("fluid slot entries are empty; ");
                // a coke oven recipe lists its log input and its charcoal and creosote outputs
                var coke = DisplayRegistry.getInstance()
                        .get(CategoryIdentifier.of(GTRecipeTypes.COKE_OVEN_RECIPES.getCategory().id)).getFirst();
                GTCEu.LOGGER.info("[REI check] coke oven inputs {} outputs {}", coke.getInputEntries(),
                        coke.getOutputEntries());
                if (coke.getInputEntries().isEmpty() || coke.getOutputEntries().size() < 2) {
                    sb.append("coke oven display misses ingredients; ");
                }
                return sb.toString();
            });

            context.setScreen(() -> new InventoryScreen(context.computeOnClient(mc -> mc.player)));
            context.waitTicks(20);
            context.runOnClient(mc -> REIRuntime.getInstance().getSearchTextField().setText("hydrogen"));
            context.waitTicks(20);
            context.takeScreenshot("gtceu_rei_hydrogen");
            context.runOnClient(mc -> REIRuntime.getInstance().getSearchTextField().setText("water"));
            context.waitTicks(20);
            context.takeScreenshot("gtceu_rei_water");
            context.runOnClient(mc -> REIRuntime.getInstance().getSearchTextField().setText(""));

            context.runOnClient(mc -> ViewSearchBuilder.builder()
                    .addCategory(CategoryIdentifier.of(GTRecipeTypes.COKE_OVEN_RECIPES.getCategory().id)).open());
            context.waitTicks(20);
            context.takeScreenshot("gtceu_rei_coke_oven");

            context.runOnClient(mc -> ViewSearchBuilder.builder().addRecipesFor(EntryStacks.of(
                    BuiltInRegistries.FLUID.getValue(GTCEu.id("hydrogen")), 81000)).open());
            context.waitTicks(20);
            context.takeScreenshot("gtceu_rei_hydrogen_recipes");
            context.getInput().setCursorPos(470, 248);
            context.waitTicks(10);
            context.takeScreenshot("gtceu_rei_fluid_tooltip");

            context.runOnClient(mc -> ViewSearchBuilder.builder()
                    .addCategory(CategoryIdentifier.of(GTCEu.id("multiblock_info"))).open());
            context.waitTicks(40);
            context.takeScreenshot("gtceu_rei_multiblock");

            context.runOnClient(mc -> ViewSearchBuilder.builder().addUsagesFor(EntryStacks.of(new ItemStack(
                    BuiltInRegistries.ITEM.getValue(GTCEu.id("lv_macerator"))))).open());
            context.waitTicks(20);
            context.takeScreenshot("gtceu_rei_macerator_uses");
            context.setScreen(() -> null);

            if (!problems.isEmpty()) throw new AssertionError("REI: " + problems);
        }
    }

    private static long gtDisplays() {
        return DisplayRegistry.getInstance().getAll().entrySet().stream()
                .filter(e -> e.getKey().getIdentifier().getNamespace().equals(GTCEu.MOD_ID))
                .mapToLong(e -> e.getValue().size()).sum();
    }
}
