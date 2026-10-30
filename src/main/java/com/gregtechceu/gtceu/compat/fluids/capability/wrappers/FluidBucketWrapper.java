package com.gregtechceu.gtceu.compat.fluids.capability.wrappers;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandlerItem;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Treats a bucket item as a single 1000 mB tank. */
public class FluidBucketWrapper implements IFluidHandlerItem {

    protected ItemStack container;

    public FluidBucketWrapper(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return !fluid.getFluid().getBucket().getDefaultInstance().isEmpty() &&
                fluid.getFluid().getBucket() != Items.AIR;
    }

    public FluidStack getFluid() {
        if (container.getItem() instanceof BucketItem bucket) {
            Fluid content = bucket.getContent();
            if (content != Fluids.EMPTY) return new FluidStack(content, FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }

    protected void setFluid(FluidStack fluid) {
        container = fluid.isEmpty() ? new ItemStack(Items.BUCKET) : new ItemStack(fluid.getFluid().getBucket());
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
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME ||
                !getFluid().isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }
        if (action.execute()) setFluid(resource);
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME) return FluidStack.EMPTY;
        FluidStack contained = getFluid();
        if (contained.isEmpty() || !FluidStack.isSameFluidSameComponents(contained, resource)) {
            return FluidStack.EMPTY;
        }
        if (action.execute()) setFluid(FluidStack.EMPTY);
        return contained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < FluidType.BUCKET_VOLUME) return FluidStack.EMPTY;
        FluidStack contained = getFluid();
        if (contained.isEmpty()) return FluidStack.EMPTY;
        if (action.execute()) setFluid(FluidStack.EMPTY);
        return contained;
    }
}
