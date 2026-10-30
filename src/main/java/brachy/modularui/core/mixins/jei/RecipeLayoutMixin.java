package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.gui.recipes.RecipeLayout;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = RecipeLayout.class, remap = false)
public abstract class RecipeLayoutMixin<R> implements IRecipeLayoutDrawable<R> {

    @WrapOperation(method = "drawOverlays",
                   at = @At(value = "INVOKE", target = "Lmezz/jei/library/gui/recipes/RecipeLayout;getSlotUnderMouse(DD)Ljava/util/Optional;"))
    private Optional<RecipeSlotUnderMouse> modularui$disableIngredientTooltipRender(RecipeLayout<R> self, double mouseX, double mouseY,
                                                                                    Operation<Optional<RecipeSlotUnderMouse>> original) {
        if (self.getRecipeCategory() instanceof ModularUIJeiCategory<R>){
            return Optional.empty();
        }
        return original.call(self, mouseX, mouseY);
    }

    // JEI's constructor reuses the category's argument slot by now, so the category comes from the call itself
    @WrapOperation(method = "<init>",
                   at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getWidth()I"),
                   require = 0)
    private int modularui$makeWidthRecipeDependent(IRecipeCategory<R> recipeCategory, Operation<Integer> original,
                                                   @Local(argsOnly = true) Object recipe) {
        if (recipeCategory instanceof ModularUIJeiCategory<R> muiCategory) {
            // noinspection unchecked
            return muiCategory.getWidth((R) recipe);
        }
        return original.call(recipeCategory);
    }

    @WrapOperation(method = "<init>",
                   at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getHeight()I"),
                   require = 0)
    private int modularui$makeHeightRecipeDependent(IRecipeCategory<R> recipeCategory, Operation<Integer> original,
                                                   @Local(argsOnly = true) Object recipe) {
        if (recipeCategory instanceof ModularUIJeiCategory<R> muiCategory) {
            // noinspection unchecked
            return muiCategory.getHeight((R) recipe);
        }
        return original.call(recipeCategory);
    }
}
