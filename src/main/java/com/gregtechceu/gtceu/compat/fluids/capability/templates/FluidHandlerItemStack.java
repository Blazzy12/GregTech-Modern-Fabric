package com.gregtechceu.gtceu.compat.fluids.capability.templates;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.SimpleFluidContent;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandlerItem;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/** Stores fluid in an item's {@link SimpleFluidContent} data component. */
public class FluidHandlerItemStack implements IFluidHandlerItem {

    protected final Supplier<DataComponentType<SimpleFluidContent>> componentType;
    protected ItemStack container;
    protected int capacity;

    public FluidHandlerItemStack(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemStack container,
                    int capacity) {
        this.componentType = componentType;
        this.container = container;
        this.capacity = capacity;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public FluidStack getFluid() {
        return container.getOrDefault(componentType.get(), SimpleFluidContent.EMPTY).copy();
    }

    protected void setFluid(FluidStack fluid) {
        container.set(componentType.get(), SimpleFluidContent.copyOf(fluid));
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty() || !canFillFluidType(resource)) return 0;
        FluidStack contained = getFluid();
        if (contained.isEmpty()) {
            int fillAmount = Math.min(capacity, resource.getAmount());
            if (action.execute()) setFluid(resource.copyWithAmount(fillAmount));
            return fillAmount;
        }
        if (FluidStack.isSameFluidSameComponents(contained, resource)) {
            int fillAmount = Math.min(capacity - contained.getAmount(), resource.getAmount());
            if (action.execute() && fillAmount > 0) setFluid(contained.copyWithAmount(contained.getAmount() + fillAmount));
            return fillAmount;
        }
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty() ||
                !FluidStack.isSameFluidSameComponents(resource, getFluid())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) return FluidStack.EMPTY;
        FluidStack contained = getFluid();
        if (contained.isEmpty() || !canDrainFluidType(contained)) return FluidStack.EMPTY;
        int drainAmount = Math.min(contained.getAmount(), maxDrain);
        FluidStack drained = contained.copyWithAmount(drainAmount);
        if (action.execute()) {
            int remaining = contained.getAmount() - drainAmount;
            if (remaining <= 0) setContainerToEmpty();
            else setFluid(contained.copyWithAmount(remaining));
        }
        return drained;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return true;
    }

    protected void setContainerToEmpty() {
        container.remove(componentType.get());
    }

    /** Destroys the container item when it is emptied. */
    public static class Consumable extends FluidHandlerItemStack {

        public Consumable(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemStack container,
                          int capacity) {
            super(componentType, container, capacity);
        }

        @Override
        protected void setContainerToEmpty() {
            super.setContainerToEmpty();
            container.shrink(1);
        }
    }

    /** Swaps the container for a different item when it is emptied. */
    public static class SwapEmpty extends FluidHandlerItemStack {

        protected final ItemStack emptyContainer;

        public SwapEmpty(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemStack container,
                         ItemStack emptyContainer, int capacity) {
            super(componentType, container, capacity);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected void setContainerToEmpty() {
            super.setContainerToEmpty();
            container = emptyContainer;
        }
    }
}
