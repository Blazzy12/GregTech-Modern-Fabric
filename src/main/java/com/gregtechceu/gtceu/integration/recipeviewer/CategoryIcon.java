package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

// Generic recipe viewer category icon
public class CategoryIcon {

    private @Nullable Object wrappedValue;
    private Either<Identifier, ItemStack> texture;

    public CategoryIcon(Identifier texture) {
        if (!GTCEu.isClientSide()) return;
        this.texture = Either.left(texture);
    }

    public CategoryIcon(ItemStack stack) {
        if (!GTCEu.isClientSide()) return;
        this.texture = Either.right(stack);
    }

    public Object get() {
        if (wrappedValue != null) return wrappedValue;
        wrappedValue = RecipeViewerHooks.iconFactory.apply(texture);
        return wrappedValue;
    }
}
