package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import com.gregtechceu.gtceu.compat.common.CommonHooks;
import com.gregtechceu.gtceu.compat.common.crafting.ICustomIngredient;
import com.gregtechceu.gtceu.compat.common.crafting.IngredientType;
import com.gregtechceu.gtceu.compat.fluids.FluidActionResult;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidUtil;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;
import com.gregtechceu.gtceu.compat.fluids.capability.templates.VoidFluidHandler;
import com.gregtechceu.gtceu.compat.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class FluidContainerIngredient implements ICustomIngredient {

    // spotless:off
    public static final MapCodec<FluidContainerIngredient> CODEC = SizedFluidIngredient.NESTED_CODEC.fieldOf("fluid")
            .xmap(FluidContainerIngredient::new, FluidContainerIngredient::getFluid);
    // spotless:on
    @Getter
    private final SizedFluidIngredient fluid;

    public FluidContainerIngredient(SizedFluidIngredient fluid) {
        this.fluid = fluid;
    }

    public FluidContainerIngredient(TagKey<Fluid> tag, int amount) {
        this(SizedFluidIngredient.of(tag, amount));
    }

    private java.util.List<ItemStack> cachedStacks;

    @NotNull
    @Override
    public Stream<ItemStack> getItems() {
        // a stream can only be read once, so cache the stacks
        if (cachedStacks == null)
            cachedStacks = Arrays.stream(this.fluid.getFluids())
                    .map(stack -> stack.getFluid().getBucket().getDefaultInstance())
                    .filter(s -> !s.isEmpty())
                    .toList();
        return this.cachedStacks.stream();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(stack).orElse(null);
        return handler != null && this.extractFrom(handler, true);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return GTIngredientTypes.FLUID_CONTAINER_INGREDIENT.get();
    }

    public ItemStack getExtractedStack(ItemStack input) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(input,
                VoidFluidHandler.INSTANCE,
                fluid.amount(),
                CommonHooks.getCraftingPlayer(),
                true);
        if (result.isSuccess()) {
            return result.getResult();
        }
        return input;
    }

    public boolean extractFrom(IFluidHandler handler, boolean simulate) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack inTank = handler.getFluidInTank(tank);
            if (fluid.test(inTank)) {
                FluidStack toExtract = inTank.copyWithAmount(fluid.amount());
                FluidStack extractedSim = handler.drain(toExtract, IFluidHandler.FluidAction.SIMULATE);
                if (extractedSim.getAmount() >= fluid.amount()) {
                    if (!simulate)
                        handler.drain(toExtract, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
            }
        }
        return false;
    }
}
