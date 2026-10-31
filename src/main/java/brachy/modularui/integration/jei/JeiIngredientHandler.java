package brachy.modularui.integration.jei;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;

import net.minecraft.world.item.ItemStack;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.JEIFluids;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;

import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static brachy.modularui.integration.jei.ModularUIJeiPlugin.jeiHelpers;

public class JeiIngredientHandler {

    /**
     * JEI on Fabric represents fluids as {@link IJeiFluidIngredient} (amounts in droplets), while ModularUI and GT use
     * the compat {@link FluidStack} (millibuckets). Anything handed to JEI goes through this; other types pass through.
     */
    public static Object toJei(Object ingredient) {
        return ingredient instanceof FluidStack fluid ? JEIFluids.of(fluid) : ingredient;
    }

    /** The reverse of {@link #toJei}, for ingredients JEI hands back (dragged, clicked). */
    public static Object fromJei(Object ingredient) {
        return ingredient instanceof IJeiFluidIngredient fluid ? JEIFluids.toGT(fluid) : ingredient;
    }

    public static List<ItemStack> toJeiIngredient(ItemEntryList list) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<FluidStack> toJeiIngredient(FluidEntryList list) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable ClickableIngredient<T> getJEIStackClickable(T stack, int x, int y, int w, int h) {
        return jeiHelpers.getIngredientManager().createTypedIngredient((T) toJei(stack))
                .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, new ImmutableRect2i(x, y, w, h)))
                .orElse(null);
    }

    public static List<ClickableIngredient<ItemStack>> toJeiIngredientClickable(ItemEntryList list, int x, int y, int w, int h) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> getJEIStackClickable(stack, x, y, w, h))
                .collect(Collectors.toList());
    }

    public static List<ClickableIngredient<FluidStack>> toJeiIngredientClickable(FluidEntryList list, int x, int y, int w, int h) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> getJEIStackClickable(stack, x, y, w, h))
                .toList();
    }
}
