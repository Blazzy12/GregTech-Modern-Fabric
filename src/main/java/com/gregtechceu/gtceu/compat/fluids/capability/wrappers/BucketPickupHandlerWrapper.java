package com.gregtechceu.gtceu.compat.fluids.capability.wrappers;

import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.Nullable;

/** Drains a fluid source block in the world through its {@link BucketPickup} behaviour. */
public class BucketPickupHandlerWrapper implements IFluidHandler {

    protected final @Nullable Player player;
    protected final BucketPickup bucketPickupHandler;
    protected final Level world;
    protected final BlockPos blockPos;

    public BucketPickupHandlerWrapper(@Nullable Player player, BucketPickup bucketPickupHandler, Level world,
                                      BlockPos blockPos) {
        this.player = player;
        this.bucketPickupHandler = bucketPickupHandler;
        this.world = world;
        this.blockPos = blockPos;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank == 0) {
            FluidState state = world.getFluidState(blockPos);
            if (!state.isEmpty() && state.isSource()) return new FluidStack(state.getType(), FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
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
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.getAmount() < FluidType.BUCKET_VOLUME) return FluidStack.EMPTY;
        FluidStack contained = getFluidInTank(0);
        if (contained.isEmpty() || !FluidStack.isSameFluid(contained, resource)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain < FluidType.BUCKET_VOLUME) return FluidStack.EMPTY;
        FluidStack contained = getFluidInTank(0);
        if (contained.isEmpty()) return FluidStack.EMPTY;
        if (action.simulate()) return contained;
        ItemStack bucket = bucketPickupHandler.pickupBlock(player, world, blockPos, world.getBlockState(blockPos));
        if (bucket.getItem() instanceof BucketItem bucketItem) {
            return new FluidStack(bucketItem.getContent(), FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }
}
