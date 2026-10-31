package com.gregtechceu.gtceu.integration.recipeviewer.rei;

import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.rei.recipe.ModularUIReiCategory;
import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A GT category in REI. Like the JEI ones, each display is a ModularUI widget (recipe, ore vein, multiblock preview,
 * ...) drawn by ModularUI, while its inputs and outputs are listed separately for REI's recipe and usage lookups.
 */
public class GTReiCategory<T> extends ModularUIReiCategory<GTReiCategory.GTDisplay<T>> {

    private final CategoryIdentifier<GTDisplay<T>> id;
    private final Supplier<Component> title;
    private final Supplier<Renderer> icon;
    private final Function<T, IWidget> widget;
    private final int width, maxHeight;

    public GTReiCategory(Identifier id, Supplier<Component> title, Supplier<Renderer> icon,
                         Function<T, IWidget> widget, int width, int maxHeight) {
        this.id = CategoryIdentifier.of(id);
        this.title = title;
        this.icon = icon;
        this.widget = widget;
        this.width = width;
        this.maxHeight = maxHeight;
    }

    public GTDisplay<T> display(T value, Identifier id, List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        return new GTDisplay<>(id, () -> widget.apply(value), this.id, inputs, outputs);
    }

    @Override
    public CategoryIdentifier<GTDisplay<T>> getCategoryIdentifier() {
        return id;
    }

    @Override
    public Component getTitle() {
        return title.get();
    }

    @Override
    public Renderer getIcon() {
        return icon.get();
    }

    @Override
    public int getFallbackDisplayWidth() {
        return width;
    }

    @Override
    public int getMaxDisplayHeight() {
        return maxHeight;
    }

    public static Renderer icon(ItemStack stack) {
        return EntryStacks.of(stack);
    }

    public static Renderer icon(CategoryIcon icon) {
        return icon.getTexture().map(GTReiCategory::icon, GTReiCategory::icon);
    }

    public static Renderer icon(Identifier texture) {
        return (graphics, bounds, mouseX, mouseY, delta) -> graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                bounds.getCenterX() - 8, bounds.getCenterY() - 8, 0, 0, 16, 16, 16, 16);
    }

    public static class GTDisplay<T> extends ModularUIReiDisplay {

        private final List<EntryIngredient> inputs, outputs;

        GTDisplay(Identifier id, Supplier<IWidget> ui, CategoryIdentifier<?> category,
                  List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
            super(id, ui, category);
            this.inputs = inputs;
            this.outputs = outputs;
        }

        @Override
        public List<EntryIngredient> getInputEntries() {
            return inputs;
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            return outputs;
        }
    }
}
