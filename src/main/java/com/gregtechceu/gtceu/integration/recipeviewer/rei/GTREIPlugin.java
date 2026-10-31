package com.gregtechceu.gtceu.integration.recipeviewer.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.GTOreByProduct;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreProcessingRecipeWidget;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ProgrammedCircuitRecipeWidget;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.rei.REIStackConverter;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rawOre;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;

/**
 * GT's recipes and info pages in REI, the counterpart of {@code GTJEIPlugin}: one category per GT recipe category
 * plus multiblock previews, ore processing diagrams, ore veins, bedrock fluids and ores, and programmed circuits.
 * Registered through the {@code rei_client} entrypoint.
 */
public class GTREIPlugin implements REIClientPlugin {

    private final Map<GTRecipeCategory, GTReiCategory<GTRecipe>> recipeCategories = new IdentityHashMap<>();
    private GTReiCategory<MultiblockMachineDefinition> multiblockInfo;
    private GTReiCategory<Material> oreProcessing;
    private GTReiCategory<GTOreDefinition> oreVeins;
    private GTReiCategory<BedrockFluidDefinition> bedrockFluids;
    private GTReiCategory<BedrockOreDefinition> bedrockOres;
    private GTReiCategory<Object> programmedCircuit;

    @Override
    public void registerCategories(CategoryRegistry registry) {
        recipeCategories.clear();
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (!category.shouldRegisterDisplays()) continue;
            var reiCategory = new GTReiCategory<GTRecipe>(category.id, () -> title(category),
                    () -> GTReiCategory.icon(category.getIcon()), GTRecipeViewerWidget::new, 250, 250);
            recipeCategories.put(category, reiCategory);
            registry.add(reiCategory);
        }
        multiblockInfo = add(registry, new GTReiCategory<>(GTCEu.id("multiblock_info"),
                () -> Component.translatable("gtceu.jei.multiblock_info"),
                () -> GTReiCategory.icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack()),
                v -> new MultiblockPreviewWidget(v, null, 200, 180), 200, 180));
        oreProcessing = add(registry, new GTReiCategory<>(GTCEu.id("ore_processing_diagram"),
                () -> Component.translatable("gtceu.jei.ore_processing_diagram"),
                () -> GTReiCategory.icon(ChemicalHelper.get(rawOre, GTMaterials.Iron)),
                OreProcessingRecipeWidget::new, 180, 180));
        oreVeins = add(registry, new GTReiCategory<>(GTCEu.id("ore_vein_diagram"),
                () -> Component.translatable("gtceu.jei.ore_vein_diagram"),
                () -> GTReiCategory.icon(ChemicalHelper.get(rawOre, GTMaterials.Iron)),
                OreVeinRecipeWidget::new, 180, 300));
        bedrockFluids = add(registry, new GTReiCategory<>(GTCEu.id("bedrock_fluid_diagram"),
                () -> Component.translatable("gtceu.jei.bedrock_fluid_diagram"),
                () -> GTReiCategory.icon(GTMaterials.Oil.value().getBucket().getDefaultInstance()),
                OreVeinRecipeWidget::new, 180, 250));
        bedrockOres = add(registry, new GTReiCategory<>(GTCEu.id("bedrock_ore_diagram"),
                () -> Component.translatable("gtceu.jei.bedrock_ore_diagram"),
                () -> GTReiCategory.icon(Items.RAW_IRON.getDefaultInstance()),
                OreVeinRecipeWidget::new, 180, 300));
        programmedCircuit = add(registry, new GTReiCategory<>(GTCEu.id("programmed_circuit"),
                () -> Component.translatable("gtceu.jei.programmed_circuit"),
                () -> GTReiCategory.icon(IntCircuitBehaviour.stack(0)),
                $ -> new ProgrammedCircuitRecipeWidget(), 250, 250));

        registerWorkstations(registry);
    }

    private static <T> GTReiCategory<T> add(CategoryRegistry registry, GTReiCategory<T> category) {
        registry.add(category);
        return category;
    }

    /** Categories have no names of their own in GT's lang files; the default ones are named like their type. */
    private static Component title(GTRecipeCategory category) {
        String typeName = net.minecraft.locale.Language.getInstance()
                .getOrDefault(category.getRecipeType().getTranslationKey());
        return Component.translatableWithFallback(category.getLanguageKey(), typeName);
    }

    private void registerWorkstations(CategoryRegistry registry) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (GTRecipeCategory category : type.getCategories()) {
                    if (!category.isXEIVisible() && !GTCEu.isDev()) continue;
                    CategoryIdentifier<?> id = category == GTRecipeTypes.FURNACE_RECIPES.getCategory() ?
                            BuiltinPlugin.SMELTING : idOf(category);
                    if (id != null) registry.addWorkstations(id, EntryIngredients.of(machine.asStack()));
                }
            }
        }
        var lcr = idOf(GTRecipeTypes.CHEMICAL_RECIPES.getCategory());
        if (lcr != null) registry.addWorkstations(lcr, EntryIngredients.of(GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack()));

        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams) {
            for (var machine : List.of(MACERATOR, ORE_WASHER, THERMAL_CENTRIFUGE, CENTRIFUGE, CHEMICAL_BATH,
                    ELECTROMAGNETIC_SEPARATOR, SIFTER)) {
                registry.addWorkstations(oreProcessing.getCategoryIdentifier(),
                        EntryIngredients.of(machine[GTValues.LV].asStack()));
            }
        }
        for (var prospector : List.of(GTItems.PROSPECTOR_LV, GTItems.PROSPECTOR_HV, GTItems.PROSPECTOR_LuV)) {
            registry.addWorkstations(oreVeins.getCategoryIdentifier(), EntryIngredients.of(prospector.asStack()));
        }
        for (var prospector : List.of(GTItems.PROSPECTOR_HV, GTItems.PROSPECTOR_LuV)) {
            registry.addWorkstations(bedrockFluids.getCategoryIdentifier(), EntryIngredients.of(prospector.asStack()));
            if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
                registry.addWorkstations(bedrockOres.getCategoryIdentifier(), EntryIngredients.of(prospector.asStack()));
            }
        }
        registry.addWorkstations(programmedCircuit.getCategoryIdentifier(),
                EntryIngredients.of(IntCircuitBehaviour.stack(0)));
    }

    private CategoryIdentifier<?> idOf(GTRecipeCategory category) {
        var reiCategory = recipeCategories.get(category);
        return reiCategory == null ? null : reiCategory.getCategoryIdentifier();
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // main categories first, so their recipe types build their representative recipes before subcategories
        List<GTRecipeCategory> subCategories = new ArrayList<>();
        for (var entry : recipeCategories.entrySet()) {
            GTRecipeCategory category = entry.getKey();
            GTRecipeType type = category.getRecipeType();
            if (category == type.getCategory()) {
                type.buildRepresentativeRecipes();
                addRecipes(registry, entry.getValue(), type.getRecipesInCategory(category));
            } else {
                subCategories.add(category);
            }
        }
        for (GTRecipeCategory category : subCategories) {
            addRecipes(registry, recipeCategories.get(category), category.getRecipeType().getRecipesInCategory(category));
        }

        for (var definition : GTRegistries.MACHINES) {
            if (definition instanceof MultiblockMachineDefinition multiblock && multiblock.isRenderXEIPreview()) {
                List<EntryIngredient> inputs = new ArrayList<>();
                for (ItemStack stack : MultiblockPreviewWidget.initializeContainedBlocks(multiblock)) {
                    inputs.add(EntryIngredients.of(stack));
                }
                registry.add(multiblockInfo.display(multiblock, multiblock.getId(), inputs,
                        List.of(EntryIngredients.of(multiblock.getBlock()))));
            }
        }

        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams) {
            for (Material material : GTRegistries.MATERIALS) {
                if (!material.hasProperty(PropertyKey.ORE) || material.hasFlag(MaterialFlags.NO_ORE_PROCESSING_TAB)) {
                    continue;
                }
                GTOreByProduct byproducts = new GTOreByProduct(material);
                List<EntryIngredient> inputs = new ArrayList<>();
                byproducts.getItemInputs().forEach(list -> inputs.add(items(list)));
                byproducts.getFluidInputs().forEach(list -> inputs.add(fluids(list)));
                List<EntryIngredient> outputs = new ArrayList<>();
                byproducts.getItemOutputs().forEach(stack -> outputs.add(EntryIngredients.of(stack)));
                registry.add(oreProcessing.display(material, material.getResourceLocation(), inputs, outputs));
            }
        }

        var level = Minecraft.getInstance().level;
        if (level != null) {
            var registries = level.registryAccess();
            addWorldgen(registry, registries.lookupOrThrow(GTRegistries.Keys.ORE_VEIN), oreVeins,
                    ore -> dimensionMarkers(OreVeinRecipeWidget.getDimensionMarkers(ore.dimensionFilter())),
                    ore -> OreVeinRecipeWidget.getContainedOresAndBlocks(ore).stream().map(EntryIngredients::of).toList());
            addWorldgen(registry, registries.lookupOrThrow(GTRegistries.Keys.BEDROCK_FLUID), bedrockFluids,
                    fluid -> dimensionMarkers(OreVeinRecipeWidget.getDimensionMarkers(fluid.dimensionFilter)),
                    fluid -> List.of(EntryIngredient.of(EntryStacks.of(fluid.getStoredFluid(),
                            dev.architectury.hooks.fluid.FluidStackHooks.bucketAmount()))));
            if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
                addWorldgen(registry, registries.lookupOrThrow(GTRegistries.Keys.BEDROCK_ORE), bedrockOres,
                        ore -> dimensionMarkers(OreVeinRecipeWidget.getDimensionMarkers(ore.dimensionFilter)),
                        ore -> OreVeinRecipeWidget.getRawMaterialList(ore).stream().map(EntryIngredients::of).toList());
            }
        }

        registry.add(programmedCircuit.display(new Object(), GTCEu.id("programmed_circuit"), List.of(),
                List.of(EntryIngredients.of(IntCircuitBehaviour.stack(0)))));
    }

    private static void addRecipes(DisplayRegistry registry, GTReiCategory<GTRecipe> category,
                                   Iterable<GTRecipe> recipes) {
        for (GTRecipe recipe : recipes) {
            registry.add(category.display(recipe, recipe.getId(), ingredients(recipe, true),
                    ingredients(recipe, false)));
        }
    }

    /** A recipe's item and fluid inputs or outputs, one REI ingredient (with its alternatives) per recipe slot. */
    private static List<EntryIngredient> ingredients(GTRecipe recipe, boolean inputs) {
        List<EntryIngredient> entries = new ArrayList<>();
        var items = inputs ? recipe.getInputContents(ItemRecipeCapability.CAP) :
                recipe.getOutputContents(ItemRecipeCapability.CAP);
        for (var content : items) {
            entries.add(items(ItemRecipeCapability.mapIngredientToEntryList(
                    ItemRecipeCapability.CAP.of(content.content()))));
        }
        var fluids = inputs ? recipe.getInputContents(FluidRecipeCapability.CAP) :
                recipe.getOutputContents(FluidRecipeCapability.CAP);
        for (var content : fluids) {
            entries.add(fluids(FluidRecipeCapability.mapIngredientToEntryList(
                    FluidRecipeCapability.CAP.of(content.content()))));
        }
        return entries;
    }

    private static EntryIngredient items(ItemEntryList list) {
        return REIStackConverter.ITEM.convertTo(list, 1f, UnaryOperator.identity());
    }

    private static EntryIngredient fluids(FluidEntryList list) {
        return REIStackConverter.FLUID.convertTo(list, 1f, UnaryOperator.identity());
    }

    private static List<EntryIngredient> dimensionMarkers(
            com.gregtechceu.gtceu.api.data.DimensionMarker[] markers) {
        List<EntryIngredient> entries = new ArrayList<>();
        for (var marker : markers) entries.add(EntryIngredients.of(marker.getIcon()));
        return entries;
    }

    private static <T> void addWorldgen(DisplayRegistry registry, Registry<T> entries, GTReiCategory<T> category,
                                        java.util.function.Function<T, List<EntryIngredient>> inputs,
                                        java.util.function.Function<T, List<EntryIngredient>> outputs) {
        for (T value : entries) {
            Identifier id = entries.getKey(value);
            if (id == null) continue;
            registry.add(category.display(value, id, inputs.apply(value), outputs.apply(value)));
        }
    }

    @Override
    public void registerEntryRenderers(EntryRendererRegistry registry) {
        registry.register(VanillaEntryTypes.FLUID, (entry, last) -> new ReiFluidRenderer(last));
    }

    /** One fluid entry per potion, as GT's JEI plugin adds them. */
    @Override
    public void registerEntries(EntryRegistry registry) {
        BuiltInRegistries.POTION.listElements().forEach(potion -> {
            FluidStack stack = PotionFluid.of(1000, potion);
            registry.addEntry(EntryStacks.of(dev.architectury.fluid.FluidStack.create(stack.getFluid(),
                    dev.architectury.hooks.fluid.FluidStackHooks.bucketAmount(), stack.getComponentsPatch())));
        });
    }
}
