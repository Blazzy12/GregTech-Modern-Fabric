package com.gregtechceu.gtceu.compat.fluids;

import com.gregtechceu.gtceu.compat.capabilities.Capabilities;
import com.gregtechceu.gtceu.compat.common.SoundActions;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandlerItem;
import com.gregtechceu.gtceu.compat.items.ItemHandlerHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class FluidUtil {

    private FluidUtil() {}

    public static Optional<IFluidHandlerItem> getFluidHandler(ItemStack stack) {
        return Optional.ofNullable(Capabilities.FluidHandler.ITEM.getCapability(stack.copyWithCount(1), null));
    }

    public static Optional<IFluidHandler> getFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return Optional.ofNullable(Capabilities.FluidHandler.BLOCK.getCapability(level, pos, null, null, side));
    }

    public static Optional<FluidStack> getFluidContained(ItemStack container) {
        if (container.isEmpty()) return Optional.empty();
        return getFluidHandler(container)
                .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE))
                .filter(stack -> !stack.isEmpty());
    }

    /** Moves up to {@code maxAmount} from source to destination. */
    public static FluidStack tryFluidTransfer(IFluidHandler destination, IFluidHandler source, int maxAmount,
                                              boolean doTransfer) {
        FluidStack drainable = source.drain(maxAmount, IFluidHandler.FluidAction.SIMULATE);
        if (drainable.isEmpty()) return FluidStack.EMPTY;
        int fillable = destination.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return FluidStack.EMPTY;
        FluidStack toDrain = drainable.copyWithAmount(fillable);
        if (!doTransfer) return toDrain;
        FluidStack drained = source.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return FluidStack.EMPTY;
        int filled = destination.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        return drained.copyWithAmount(filled);
    }

    public static FluidStack tryFluidTransfer(IFluidHandler destination, IFluidHandler source, FluidStack resource,
                                              boolean doTransfer) {
        FluidStack drainable = source.drain(resource, IFluidHandler.FluidAction.SIMULATE);
        if (drainable.isEmpty() || !FluidStack.isSameFluidSameComponents(drainable, resource)) {
            return FluidStack.EMPTY;
        }
        return tryFluidTransfer(destination, source, drainable.getAmount(), doTransfer);
    }

    /** Fills the container from {@code fluidSource}. The returned stack is the filled container. */
    public static FluidActionResult tryFillContainer(ItemStack container, IFluidHandler fluidSource, int maxAmount,
                                                     @Nullable Player player, boolean doFill) {
        ItemStack copy = container.copyWithCount(1);
        IFluidHandlerItem handler = Capabilities.FluidHandler.ITEM.getCapability(copy, null);
        if (handler == null) return FluidActionResult.FAILURE;
        FluidStack simulated = tryFluidTransfer(handler, fluidSource, maxAmount, false);
        if (simulated.isEmpty()) return FluidActionResult.FAILURE;
        if (doFill) {
            FluidStack moved = tryFluidTransfer(handler, fluidSource, maxAmount, true);
            if (moved.isEmpty()) return FluidActionResult.FAILURE;
            playSound(player, moved, true);
        } else {
            handler.fill(simulated, IFluidHandler.FluidAction.EXECUTE);
        }
        return new FluidActionResult(handler.getContainer());
    }

    /** Empties the container into {@code fluidDestination}. The returned stack is the emptied container. */
    public static FluidActionResult tryEmptyContainer(ItemStack container, IFluidHandler fluidDestination,
                                                      int maxAmount, @Nullable Player player, boolean doDrain) {
        ItemStack copy = container.copyWithCount(1);
        IFluidHandlerItem handler = Capabilities.FluidHandler.ITEM.getCapability(copy, null);
        if (handler == null) return FluidActionResult.FAILURE;
        if (doDrain) {
            FluidStack moved = tryFluidTransfer(fluidDestination, handler, maxAmount, true);
            if (moved.isEmpty()) return FluidActionResult.FAILURE;
            playSound(player, moved, false);
        } else {
            FluidStack simulated = tryFluidTransfer(fluidDestination, handler, maxAmount, false);
            if (simulated.isEmpty()) return FluidActionResult.FAILURE;
            handler.drain(simulated, IFluidHandler.FluidAction.EXECUTE);
        }
        return new FluidActionResult(handler.getContainer());
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos,
                                                   @Nullable Direction side) {
        return getFluidHandler(level, pos, side).map(handler -> interactWithFluidHandler(player, hand, handler))
                .orElse(false);
    }

    /**
     * Fills the held container from the handler, or empties it into the handler, replacing the held stack.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, IFluidHandler handler) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return false;

        FluidActionResult result = tryFillContainer(held, handler, Integer.MAX_VALUE, player, true);
        if (!result.isSuccess()) result = tryEmptyContainer(held, handler, Integer.MAX_VALUE, player, true);
        if (!result.isSuccess()) return false;

        if (player.getAbilities().instabuild) return true;
        if (held.getCount() == 1) {
            player.setItemInHand(hand, result.getResult());
        } else {
            held.shrink(1);
            ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
        }
        return true;
    }

    private static void playSound(@Nullable Player player, FluidStack fluid, boolean fill) {
        if (player == null) return;
        SoundEvent sound = fluid.getFluidType().getSound(fluid,
                fill ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
        if (sound != null) {
            player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), sound,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
