package com.gregtechceu.gtceu.compat.registries;


import com.gregtechceu.gtceu.compat.common.crafting.IngredientType;
import com.gregtechceu.gtceu.compat.fluids.FluidType;
import com.gregtechceu.gtceu.compat.fluids.crafting.FluidIngredientType;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Registries NeoForge adds to the game. Ingredient types are additionally bound to Fabric's custom ingredient
 * serializers as they are registered.
 */
public final class NeoForgeRegistries {

    public static final class Keys {

        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = key("fluid_type");
        public static final ResourceKey<Registry<IngredientType<?>>> INGREDIENT_TYPES = key("ingredient_type");
        public static final ResourceKey<Registry<FluidIngredientType<?>>> FLUID_INGREDIENT_TYPES = key(
                "fluid_ingredient_type");


        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("gtceu", name));
        }

        private Keys() {}
    }

    public static final Registry<FluidType> FLUID_TYPES = FabricRegistryBuilder
            .create(Keys.FLUID_TYPES).buildAndRegister();
    public static final Registry<IngredientType<?>> INGREDIENT_TYPES = FabricRegistryBuilder
            .create(Keys.INGREDIENT_TYPES).buildAndRegister();
    public static final Registry<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = FabricRegistryBuilder
            .create(Keys.FLUID_INGREDIENT_TYPES).buildAndRegister();


    static {
        RegistryEntryAddedCallback.event(INGREDIENT_TYPES)
                .register((rawId, id, type) -> IngredientType.bind(id, type));
        Registry.register(INGREDIENT_TYPES, Identifier.fromNamespaceAndPath("neoforge", "components"),
                com.gregtechceu.gtceu.compat.common.crafting.DataComponentIngredient.TYPE);
        Registry.register(INGREDIENT_TYPES, Identifier.fromNamespaceAndPath("neoforge", "intersection"),
                com.gregtechceu.gtceu.compat.common.crafting.IntersectionIngredient.TYPE);
        FluidIngredientType.registerBuiltins(FLUID_INGREDIENT_TYPES);
    }

    /** Forces the static registries to be created during mod initialisation. */
    public static void init() {}

    private NeoForgeRegistries() {}
}
