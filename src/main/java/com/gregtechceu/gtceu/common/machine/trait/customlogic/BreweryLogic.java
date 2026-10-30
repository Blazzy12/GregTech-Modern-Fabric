package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;

// TODO: Make these static recipes
public enum BreweryLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    private static final Function<Fluid, TagKey<Fluid>> FLUID_TAGS = Util
            .memoize(fluid -> TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid).getPath()));
    private static final Function<Mix, FluidStack> MIX_INPUTS = Util
            .memoize(mix -> PotionFluidHelper.getFluidFromPotion(mix.from(), PotionFluidHelper.MB_PER_RECIPE));

    /**
     * One potion-to-potion brewing step. Brewing is data-driven in 26.x ({@link BrewingRecipe}), so these are derived
     * from the brewing recipes that turn one drinkable potion into another; container transformations (splash,
     * lingering) have no fluid equivalent and are skipped.
     */
    public record Mix(Holder<Potion> from, Ingredient ingredient, Holder<Potion> to) {}

    private static @Nullable RecipeManager cachedManager;
    private static List<Mix> cachedMixes = List.of();

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return null;
        List<Mix> mixes = getMixes();
        if (mixes.isEmpty()) return null;
        List<RecipeHandlerList> distinct = new ArrayList<>();
        List<IRecipeHandler<?>> notDistinctItems = new ArrayList<>();
        List<IRecipeHandler<?>> notDistinctFluids = new ArrayList<>();

        for (var handlerList : handlerLists) {
            if (handlerList.isDistinct()) {
                distinct.add(handlerList);
            } else {
                notDistinctItems.addAll(handlerList.getCapability(ItemRecipeCapability.CAP));
                notDistinctFluids.addAll(handlerList.getCapability(FluidRecipeCapability.CAP));
            }
        }

        if (distinct.isEmpty() && notDistinctItems.isEmpty() && notDistinctFluids.isEmpty()) return null;

        List<ItemStack> itemStacks = new ArrayList<>();
        List<FluidStack> fluidStacks = new ArrayList<>();

        for (var handlerList : distinct) {
            itemStacks.clear();
            fluidStacks.clear();
            if (!collect(handlerList, itemStacks, fluidStacks)) continue;

            for (var itemStack : itemStacks) {
                for (Mix mix : mixes) {
                    // test item ingredient first
                    if (!mix.ingredient().test(itemStack)) {
                        continue;
                    }
                    FluidStack fromFluid = MIX_INPUTS.apply(mix);
                    // then match fluid input
                    for (var fluidStack : fluidStacks) {
                        if (testMixFluid(fluidStack, fromFluid)) {
                            return potionRecipe(mix, fromFluid);
                        }
                    }
                }
            }
        }

        if (notDistinctItems.isEmpty() && notDistinctFluids.isEmpty()) return null;

        itemStacks.clear();
        fluidStacks.clear();
        collect(notDistinctItems, notDistinctFluids, itemStacks, fluidStacks);
        if (itemStacks.isEmpty() && fluidStacks.isEmpty()) return null;

        ReferenceOpenHashSet<Mix> mixesWithIngredient = new ReferenceOpenHashSet<>();
        Reference2ObjectOpenHashMap<Mix, FluidStack> mixesWithInput = new Reference2ObjectOpenHashMap<>();

        for (Mix mix : mixes) {
            for (var itemStack : itemStacks) {
                if (mix.ingredient().test(itemStack)) {
                    mixesWithIngredient.add(mix);
                    break;
                }
            }

            for (var fluidStack : fluidStacks) {
                FluidStack fromFluid = MIX_INPUTS.apply(mix);
                if (testMixFluid(fluidStack, fromFluid)) {
                    if (mixesWithIngredient.contains(mix)) {
                        return potionRecipe(mix, fromFluid);
                    } else {
                        mixesWithInput.put(mix, fromFluid);
                        break;
                    }
                }
            }
        }

        if (mixesWithIngredient.isEmpty() && mixesWithInput.isEmpty()) {
            return null;
        }

        for (var handlerList : distinct) {
            itemStacks.clear();
            fluidStacks.clear();
            collect(handlerList, itemStacks, fluidStacks);
            if (!mixesWithInput.isEmpty()) {
                for (var itemStack : itemStacks) {
                    for (var entry : mixesWithInput.reference2ObjectEntrySet()) {
                        var mix = entry.getKey();
                        var fluid = entry.getValue();
                        if (mix.ingredient().test(itemStack)) return potionRecipe(mix, fluid);
                    }
                }
            }

            if (!mixesWithIngredient.isEmpty()) {
                for (var fluidStack : fluidStacks) {
                    for (var mix : mixesWithIngredient) {
                        FluidStack fromFluid = MIX_INPUTS.apply(mix);
                        if (testMixFluid(fluidStack, fromFluid)) {
                            return potionRecipe(mix, fromFluid);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean testMixFluid(FluidStack fluidStack, FluidStack fromFluid) {
        var fromTag = FLUID_TAGS.apply(fromFluid.getFluid());
        return (fluidStack.getFluid() == fromFluid.getFluid() || fluidStack.getFluid().is(fromTag)) &&
                Objects.equals(fromFluid.getComponents(), fluidStack.getComponents());
    }

    private static String potionName(Holder<Potion> potion) {
        return potion.value().name();
    }

    private static @NotNull GTRecipe potionRecipe(Mix mix, FluidStack fromFluid) {
        FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to(), PotionFluidHelper.MB_PER_RECIPE);
        return GTRecipeTypes.BREWING_RECIPES
                .recipeBuilder("potion_vanilla_" + potionName(mix.to()))
                .inputItems(mix.ingredient())
                .inputFluids(fromFluid)
                .outputFluids(toFluid)
                .duration(400)
                .EUt(VHA[MV])
                .build();
    }

    private static boolean collect(RecipeHandlerList rhl, List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        return collect(rhl.getCapability(ItemRecipeCapability.CAP),
                rhl.getCapability(FluidRecipeCapability.CAP),
                itemStacks, fluidStacks);
    }

    private static boolean collect(List<IRecipeHandler<?>> itemHandlers, List<IRecipeHandler<?>> fluidHandlers,
                                   List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        for (var handler : itemHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    itemStacks.add(stack);
                }
            }
        }

        for (var handler : fluidHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    fluidStacks.add(stack);
                }
            }
        }

        return !(itemStacks.isEmpty() || fluidStacks.isEmpty());
    }

    @Override
    public void buildRepresentativeRecipes() {
        int index = 0;
        for (Mix mix : getMixes()) {
            FluidStack fromFluid = PotionFluidHelper.getFluidFromPotion(mix.from(), PotionFluidHelper.MB_PER_RECIPE);
            FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to(), PotionFluidHelper.MB_PER_RECIPE);

            GTRecipe recipe = GTRecipeTypes.BREWING_RECIPES
                    .recipeBuilder("potion_vanilla_" + potionName(mix.to()) + "_" + index++)
                    .inputItems(mix.ingredient())
                    .inputFluids(fromFluid)
                    .outputFluids(toFluid)
                    .duration(400)
                    // is this a good voltage?
                    .EUt(VHA[MV])
                    .build();
            // for recipe viewers to detect it's a synthetic recipe (not ever in JSON)
            recipe.setId(recipe.getId().withPrefix("/"));
            GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
        }
    }

    /**
     * Brewing recipes only exist on the logical server in 26.x, so on a remote client this is empty.
     */
    public static List<Mix> getMixes() {
        MinecraftServer server = GTCEu.getMinecraftServer();
        if (server == null) return List.of();
        RecipeManager manager = server.getRecipeManager();
        if (manager != cachedManager) {
            cachedMixes = collectMixes(manager);
            cachedManager = manager;
        }
        return cachedMixes;
    }

    private static List<Mix> collectMixes(RecipeManager manager) {
        ItemStack bottle = new ItemStack(Items.POTION);
        List<Mix> mixes = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value().getType() != RecipeType.BREWING ||
                    !(holder.value() instanceof BrewingRecipe brew)) {
                continue;
            }
            ItemStack output = brew.getOutput().create();
            if (!output.is(Items.POTION)) continue;
            Optional<Holder<Potion>> to = output.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                    .potion();
            PotionIngredient input = brew.getInput();
            if (to.isEmpty() || !input.ingredient().test(bottle)) continue;
            Optional<HolderSet<Potion>> from = input.potions().flatMap(PotionsPredicate::potions);
            if (from.isEmpty() || input.potions().get().effects().isPresent()) continue;
            for (Holder<Potion> potion : from.get()) {
                mixes.add(new Mix(potion, brew.getReagent().ingredient(), to.get()));
            }
        }
        return List.copyOf(mixes);
    }
}
