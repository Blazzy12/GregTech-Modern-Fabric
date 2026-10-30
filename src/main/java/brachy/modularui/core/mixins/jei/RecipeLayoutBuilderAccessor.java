package brachy.modularui.core.mixins.jei;

import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipeLayoutBuilder.class, remap = false)
public interface RecipeLayoutBuilderAccessor {

    @Accessor("ingredientManager")
    IIngredientManagerInternal modularui$getIngredientManager();
}
