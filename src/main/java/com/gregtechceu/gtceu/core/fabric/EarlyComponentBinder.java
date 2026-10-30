package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;

/**
 * In 26.x, registry elements (items above all) only get their default data components when the server loads its
 * data, so an {@code ItemStack} cannot be made while mods initialise. GregTech makes stacks all through its
 * content setup, like it could on 1.21.1, so the defaults are bound early here from vanilla's built-in data
 * registries. Loading data later binds them again, with the real data packs.
 */
public final class EarlyComponentBinder {

    private static boolean binding;

    private EarlyComponentBinder() {}

    public static boolean isBinding() {
        return binding;
    }

    public static void bind() {
        long start = System.currentTimeMillis();
        binding = true;
        try {
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createWorldLookup())
                    .forEach(DataComponentInitializers.PendingComponents::apply);
        } catch (RuntimeException e) {
            GTCEu.LOGGER.error("Failed to bind default data components early", e);
        } finally {
            binding = false;
        }
        GTCEu.LOGGER.debug("Bound default data components in {}ms", System.currentTimeMillis() - start);
    }
}
