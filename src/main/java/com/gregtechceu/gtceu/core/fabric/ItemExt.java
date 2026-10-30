package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.common.ItemAbilities;
import com.gregtechceu.gtceu.compat.common.ItemAbility;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;

/** Injected into {@link Item}. Items override {@link #canPerformAction} to declare tool abilities. */
public interface ItemExt extends com.gregtechceu.gtceu.compat.common.extensions.IItemExtension {

    default boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        Item item = (Item) this;
        if (stack.is(ItemTags.AXES)) return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability);
        if (stack.is(ItemTags.PICKAXES)) return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability);
        if (stack.is(ItemTags.SHOVELS)) return ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability);
        if (stack.is(ItemTags.HOES)) return ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability);
        if (stack.is(ItemTags.SWORDS)) return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(ability);
        if (item instanceof ShearsItem) return ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(ability);
        if (item instanceof ShieldItem) return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(ability);
        if (item instanceof FishingRodItem) return ItemAbilities.DEFAULT_FISHING_ROD_ACTIONS.contains(ability);
        if (item instanceof TridentItem) return ItemAbilities.DEFAULT_TRIDENT_ACTIONS.contains(ability);
        if (item instanceof BrushItem) return ItemAbilities.DEFAULT_BRUSH_ACTIONS.contains(ability);
        if (item instanceof FlintAndSteelItem) return ItemAbilities.DEFAULT_FLINT_ACTIONS.contains(ability);
        if (item instanceof FireChargeItem) return ItemAbilities.DEFAULT_FIRECHARGE_ACTIONS.contains(ability);
        if (item instanceof SpyglassItem) return ItemAbilities.DEFAULT_SPYGLASS_ACTIONS.contains(ability);
        return false;
    }
}
