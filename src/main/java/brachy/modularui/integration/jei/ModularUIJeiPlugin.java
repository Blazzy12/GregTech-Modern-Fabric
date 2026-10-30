package brachy.modularui.integration.jei;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.jei.handler.JeiScreenHandler;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;

import com.gregtechceu.gtceu.compat.MethodsReturnNonnullByDefault;
import net.minecraft.resources.Identifier;


import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    @Getter
    private static IJeiRuntime runtime = null;
    public static IJeiHelpers jeiHelpers;

    public static boolean hasRuntime() {
        return runtime != null;
    }

    public static RecipeIngredientRole mapToJeiRole(RecipeSlotRole slotRole) {
        return switch (slotRole) {
            case INPUT -> RecipeIngredientRole.INPUT;
            case OUTPUT -> RecipeIngredientRole.OUTPUT;
            case CATALYST -> RecipeIngredientRole.CRAFTING_STATION;
            case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
        };
    }

    @Override
    public Identifier getPluginUid() {
        return ModularUI.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (!ModularUI.Mods.REI.isLoaded() && !ModularUI.Mods.EMI.isLoaded()) {
            RecipeViewerHandler.setCurrent(JeiScreenHandler.of(ScreenWrapper.class));
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        RecipeViewerHandler.setCurrent(null);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        if (ModularUI.Mods.REI.isLoaded() || ModularUI.Mods.EMI.isLoaded()) return;

        JeiScreenHandler.register(ScreenWrapper.class, registration);
        JeiScreenHandler.register(ContainerScreenWrapper.class, registration);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (ModularUI.Mods.REI.isLoaded() || ModularUI.Mods.EMI.isLoaded()) return;

        //JeiContainerHandler.register(ModularContainerMenu.class, registration);
    }

}
