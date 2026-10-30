package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.recipes.RecipeGuiLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeGuiLogic.class, remap = false)
public abstract class RecipeGuiLogicMixin {

    @Shadow
    public abstract IRecipeCategory<?> getSelectedRecipeCategory();

    // if this is a MUI category, force the gui to use the category's max height as the recipe height
    // (instead of the height of the first recipe it finds, which may be shorter than others)
    @ModifyExpressionValue(method = "getVisibleRecipeLayoutsWithButtons",
                           at = @At(value = "INVOKE",
                                    target = "Ljava/util/Optional;orElseGet(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private Object modularui$useCategoryMaxHeightForLayout(Object size) {
        if (getSelectedRecipeCategory() instanceof ModularUIJeiCategory<?> muiCategory &&
                size instanceof ImmutableSize2i recipeSize) {
            return new ImmutableSize2i(recipeSize.width(), muiCategory.getMaxHeight());
        }
        return size;
    }
}
