package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.world.item.crafting.Ingredient;
import com.gregtechceu.gtceu.compat.common.crafting.SizedIngredient;
import com.gregtechceu.gtceu.compat.fluids.crafting.FluidIngredient;
import com.gregtechceu.gtceu.compat.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.Nullable;

public class SizedIngredientExtensions {

    public static SizedFluidIngredient shrink(SizedFluidIngredient self, int amount) {
        return new SizedFluidIngredient(self.ingredient(), self.amount() - amount);
    }

    public static SizedFluidIngredient copy(SizedFluidIngredient self) {
        FluidIngredient ingredient = self.ingredient();
        if (ingredient instanceof IntProviderFluidIngredient intProv) {
            ingredient = intProv.copy();
        }
        return new SizedFluidIngredient(ingredient, self.amount());
    }

    public static SizedFluidIngredient copyWithAmount(SizedFluidIngredient self, int amount) {
        return new SizedFluidIngredient(self.ingredient(), amount);
    }

    public static @Nullable net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient getContainedCustom(SizedIngredient self) {
        return self.ingredient().getCustomIngredient();
    }

    public static SizedIngredient shrink(SizedIngredient self, int count) {
        return new SizedIngredient(self.ingredient(), self.count() - count);
    }

    public static SizedIngredient copy(SizedIngredient self) {
        if (getContainedCustom(self) instanceof IntCircuitIngredient) {
            return new SizedIngredient(self.ingredient(), self.count());
        } else if (getContainedCustom(self) instanceof IntProviderIngredient intProviderIngredient) {
            return new SizedIngredient(copyIntProvider(intProviderIngredient), self.count());
        }
        return new SizedIngredient(self.ingredient(), self.count());
    }

    public static SizedIngredient copyWithCount(SizedIngredient self, int count) {
        if (self.ingredient().getCustomIngredient() instanceof IntCircuitIngredient) {
            return new SizedIngredient(self.ingredient(), count);
        } else if (self.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProviderIngredient) {
            return new SizedIngredient(copyIntProvider(intProviderIngredient), count);
        }
        return new SizedIngredient(self.ingredient(), count);
    }

    private static Ingredient copyIntProvider(IntProviderIngredient toCopy) {
        var copied = new IntProviderIngredient(toCopy.inner, toCopy.countProvider);
        copied.sampledCount = toCopy.sampledCount;
        return copied.toVanilla();
    }
}
