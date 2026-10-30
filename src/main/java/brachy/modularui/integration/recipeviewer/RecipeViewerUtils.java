package brachy.modularui.integration.recipeviewer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class RecipeViewerUtils {

    private RecipeViewerUtils() {}

    public static final String CATEGORY_LANGUAGE_KEY_PREFIX = "gui.recipeviewer.category";

    public static Component getCategoryTitle(Identifier categoryId) {
        return Component.translatable(categoryId.toLanguageKey(CATEGORY_LANGUAGE_KEY_PREFIX));
    }

}
