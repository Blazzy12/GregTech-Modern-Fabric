package com.gregtechceu.gtceu.api.addon;

import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface IGTAddon {

    /**
     * @return this addon's GTRegistrate instance.
     */
    GTRegistrate getRegistrate();

    default void addRecipes(RecipeOutput provider) {}

    default void removeRecipes(Consumer<Identifier> consumer) {}

    /**
     * Register Material -> Casing block mappings here
     *
     * @deprecated Subscribe to the {@link MaterialCasingCollectionEvent} directly.
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    default void collectMaterialCasings(MaterialCasingCollectionEvent event) {}


    /**
     * Does this addon require high-tier content to be enabled?
     * 
     * @return if this addon requires highTier.
     */
    default boolean requiresHighTier() {
        return false;
    }
}
