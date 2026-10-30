package brachy.modularui.integration.recipeviewer.handlers;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class RecipeViewerHandler {

    private static RecipeViewerHandler current = null;

    @NotNull
    public static RecipeViewerHandler getCurrent() {
        return current != null ? current : DUMMY;
    }

    /** Called by a recipe viewer integration when it loads. */
    public static void setCurrent(@Nullable RecipeViewerHandler handler) {
        current = handler;
    }

    public abstract void setSearchFocused(boolean focused);

    public abstract boolean isSearchFocused();

    public abstract @Nullable Object getCurrentlyDragged();

    public abstract <I> RecipeViewerSlotWidget<I, ?> createRecipeViewerSlot(Class<I> ingredientClass);

    public boolean isHoveringOver(GhostIngredientSlot<?> slot) {
        Object currentlyDragged = getCurrentlyDragged();
        if (currentlyDragged == null) {
            return false;
        } else if (currentlyDragged instanceof Iterable<?> iterable) {
            for (Object dragged : iterable) {
                if (slot.castGhostIngredientIfValid(dragged) != null) {
                    return true;
                }
            }
            return false;
        } else {
            return slot.castGhostIngredientIfValid(currentlyDragged) != null;
        }
    }

    private static final RecipeViewerHandler DUMMY = new RecipeViewerHandler() {

        @Override
        public void setSearchFocused(boolean focused) {}

        @Override
        public boolean isSearchFocused() {
            return false;
        }

        @Override
        public @Nullable Object getCurrentlyDragged() {
            return null;
        }

        @Override
        public <I> RecipeViewerSlotWidget<I, ?> createRecipeViewerSlot(Class<I> ingredientClass) {
            class DummyRecipeViewerSlot extends RecipeViewerSlotWidget<I, DummyRecipeViewerSlot> {

                DummyRecipeViewerSlot(Class<I> ingredientClass) {
                    super(ingredientClass);
                }

                @Override
                protected void rebuildRealSlot() {}
            }
            return new DummyRecipeViewerSlot(ingredientClass);
        }
    };
}
