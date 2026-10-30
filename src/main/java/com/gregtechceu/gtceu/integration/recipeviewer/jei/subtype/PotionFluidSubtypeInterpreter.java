package com.gregtechceu.gtceu.integration.recipeviewer.jei.subtype;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import com.gregtechceu.gtceu.integration.recipeviewer.jei.JEIFluids;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements ISubtypeInterpreter<IJeiFluidIngredient> {

    public static final PotionFluidSubtypeInterpreter INSTANCE = new PotionFluidSubtypeInterpreter();

    private PotionFluidSubtypeInterpreter() {}

    @Override
    public @Nullable Object getSubtypeData(@NotNull IJeiFluidIngredient jeiIngredient, @NotNull UidContext context) {
        FluidStack ingredient = JEIFluids.toGT(jeiIngredient);
        PotionContents contents = ingredient.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return null;
        }
        return contents.potion()
                .orElse(null);
    }

}
