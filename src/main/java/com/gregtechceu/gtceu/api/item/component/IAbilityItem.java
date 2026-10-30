package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.item.ItemStack;
import com.gregtechceu.gtceu.compat.common.ItemAbility;

public interface IAbilityItem extends IItemComponent {

    default boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return false;
    }
}
