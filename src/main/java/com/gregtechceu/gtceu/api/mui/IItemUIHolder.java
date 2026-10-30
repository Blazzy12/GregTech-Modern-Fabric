package com.gregtechceu.gtceu.api.mui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.PlayerInventoryUIFactory;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;

public interface IItemUIHolder extends IUIHolder<PlayerInventoryGuiData<?>>, IInteractionItem {

    default boolean shouldOpenUI() {
        return true;
    }

    @Override
    default InteractionResult use(ItemStack item, Level level, Player player,
                                                   InteractionHand usedHand) {
        if (!shouldOpenUI()) return IInteractionItem.super.use(item, level, player, usedHand);

        if (!level.isClientSide()) {
            PlayerInventoryUIFactory.INSTANCE.openFromHand(player, usedHand);
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(usedHand));
    }

    @Override
    default InteractionResult useOn(UseOnContext context) {
        if (!shouldOpenUI()) return IInteractionItem.super.useOn(context);

        if (!context.getLevel().isClientSide()) {
            PlayerInventoryUIFactory.INSTANCE.openFromHand(context.getPlayer(), context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    default ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }
}
