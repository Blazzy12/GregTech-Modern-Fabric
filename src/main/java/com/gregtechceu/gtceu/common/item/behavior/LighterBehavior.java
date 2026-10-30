package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.core.fabric.IIgnitableBlock;

import com.gregtechceu.gtceu.api.item.component.IAbilityItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import com.gregtechceu.gtceu.compat.capabilities.Capabilities;
import com.gregtechceu.gtceu.compat.common.ItemAbilities;
import com.gregtechceu.gtceu.compat.common.ItemAbility;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandler;
import com.gregtechceu.gtceu.compat.fluids.capability.IFluidHandlerItem;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.AbstractCandleBlock.LIT;

public class LighterBehavior implements IDurabilityBar, IInteractionItem, IAddInformation, IAbilityItem {

    private static final IntIntPair DURABILITY_BAR_COLORS = GradientUtil.getGradient(0xF07F1D, 10);
    private final boolean usesFluid;
    private final boolean hasMultipleUses;
    private final boolean canOpen;
    private Supplier<ItemStack> destroyItem = () -> ItemStack.EMPTY;

    private int maxUses = 0;

    public LighterBehavior(boolean useFluid, boolean hasMultipleUses, boolean canOpen, Supplier<ItemStack> destroyItem,
                           int maxUses) {
        this(useFluid, hasMultipleUses, canOpen);
        this.maxUses = maxUses;
        this.destroyItem = destroyItem;
    }

    public LighterBehavior(boolean useFluid, boolean hasMultipleUses, boolean canOpen) {
        this.usesFluid = useFluid;
        this.hasMultipleUses = hasMultipleUses;
        this.canOpen = canOpen;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return ItemAbilities.DEFAULT_FLINT_ACTIONS.contains(action);
    }

    @Override
    public InteractionResult use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (!canOpen || !player.isSecondaryUseActive()) {
            return IInteractionItem.super.use(item, level, player, usedHand);
        }
        item.update(GTDataComponents.LIGHTER_OPEN, false, isOpen -> !isOpen);
        return InteractionResult.SUCCESS.heldItemTransformedTo(item);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        boolean isOpen = !canOpen || itemStack.getOrDefault(GTDataComponents.LIGHTER_OPEN, false);
        if (isOpen && (player == null || !player.isSecondaryUseActive())) {
            // check if it's "tnt-like" in that it implements the same method for igniting it
            if (block instanceof TntBlock || block instanceof IIgnitableBlock) {
                if (!consumeFuel(player, itemStack)) return InteractionResult.PASS;

                if (block instanceof IIgnitableBlock ignitable) {
                    ignitable.onCaughtFire(state, level, pos, clickedFace, player);
                } else {
                    TntBlock.prime(level, pos, player, itemStack);
                }
                FluidState fluidState = level.getFluidState(pos);
                level.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
                return InteractionResult.SUCCESS;
            }
            if ((CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state))) {
                if (!consumeFuel(player, itemStack)) return InteractionResult.PASS;

                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return InteractionResult.SUCCESS;
            }

            BlockPos offset = pos.relative(clickedFace);
            if (BaseFireBlock.canBePlacedAt(level, offset, context.getHorizontalDirection())) {
                if (!consumeFuel(player, itemStack)) return InteractionResult.PASS;

                level.playSound(player, offset, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState fireState = BaseFireBlock.getState(level, offset);
                level.setBlock(offset, fireState, Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, offset, itemStack);
                }
                if (player != null) {
                    itemStack.hurtAndBreak(1, player, context.getHand());
                } else if (level instanceof ServerLevel serverLevel) {
                    itemStack.hurtAndBreak(1, serverLevel, null, item -> {});
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        Level level = player.level();

        boolean isOpen = !canOpen || stack.getOrDefault(GTDataComponents.LIGHTER_OPEN, false);
        if (isOpen && !player.isShiftKeyDown()) {
            if (interactionTarget instanceof Creeper creeper) {
                if (!consumeFuel(player, stack)) return InteractionResult.PASS;
                level.playSound(player, creeper, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                if (!level.isClientSide()) {
                    creeper.ignite();
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public boolean consumeFuel(@Nullable Player player, ItemStack stack) {
        if (player != null && player.isCreative())
            return true;

        int usesLeft = getUsesLeft(stack) - 1;

        if (usesLeft >= 0) {
            setUsesLeft(player, stack, usesLeft);
            return true;
        }
        return false;
    }

    private int getUsesLeft(ItemStack stack) {
        if (usesFluid) {
            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM, null);
            if (handler == null) return 0;

            FluidStack fluid = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            return fluid.getAmount();
        } else if (hasMultipleUses) {
            return maxUses - stack.getOrDefault(DataComponents.DAMAGE, 0);
        } else {
            return stack.getCount();
        }
    }

    private void setUsesLeft(Player player, @NotNull ItemStack stack, final int usesLeft) {
        if (usesFluid) {
            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM, null);
            if (handler != null) {
                FluidStack fluid = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!fluid.isEmpty()) {
                    handler.drain(fluid.getAmount() - usesLeft, IFluidHandler.FluidAction.EXECUTE);
                }
            } ;
        } else if (hasMultipleUses) {
            if (usesLeft <= 0) {
                stack.shrink(1);
                ItemStack brokenStack = this.destroyItem.get();
                if (!player.addItem(brokenStack)) {
                    player.drop(brokenStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            } else {
                stack.set(DataComponents.DAMAGE, maxUses - usesLeft);
            }
        } else {
            stack.setCount(usesLeft);
        }
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        if (usesFluid) {
            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM, null);
            if (handler == null) return 0.0f;

            FluidStack fluid = handler.getFluidInTank(0);
            return (float) fluid.getAmount() / (float) handler.getTankCapacity(0);
        } else if (hasMultipleUses) {
            return (float) getUsesLeft(stack) / (float) maxUses;
        } else {
            return 0.0f;
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return usesFluid || hasMultipleUses;
    }

    @Override
    public boolean showEmptyBar(ItemStack itemStack) {
        return usesFluid || hasMultipleUses;
    }

    @Override
    public @Nullable IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
        if (hasMultipleUses && usesFluid) {
            return DURABILITY_BAR_COLORS;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component
                .translatable(usesFluid ? "behaviour.lighter.fluid.tooltip" : "behaviour.lighter.tooltip.description"));
        tooltipComponents.add(Component.translatable("behaviour.lighter.tooltip.usage"));
        if (hasMultipleUses && !usesFluid) {
            tooltipComponents.add(Component.translatable("behaviour.lighter.uses", getUsesLeft(stack)));
        }
    }

}
