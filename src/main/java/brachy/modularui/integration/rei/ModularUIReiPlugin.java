package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.integration.rei.handler.REIScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;

/** ModularUI screens in REI: exclusion zones, and dragging and clicking ingredients. Registered through the {@code rei_client} entrypoint. */
public class ModularUIReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        REIScreenHandler.register(ScreenWrapper.class, zones);
        REIScreenHandler.register(ContainerScreenWrapper.class, zones);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        REIScreenHandler.register(ScreenWrapper.class, registry);
        REIScreenHandler.register(ContainerScreenWrapper.class, registry);
        // recipe slots in ModularUI recipe displays are REI entry widgets while REI is the recipe viewer
        RecipeViewerHandler.setCurrent(REIScreenHandler.of(ScreenWrapper.class));
    }
}
