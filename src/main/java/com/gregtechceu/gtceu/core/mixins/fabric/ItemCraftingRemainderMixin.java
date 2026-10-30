package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.compat.common.extensions.IItemExtension;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Routes Fabric's stack-sensitive crafting remainder to {@link IItemExtension#getCraftingRemainingItem}, so GT
 * items (tools losing durability, fluid containers emptying) keep their remainder logic.
 */
@Mixin(Item.class)
public abstract class ItemCraftingRemainderMixin implements IItemExtension {

    public @Nullable ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        ItemStack remainder = getCraftingRemainingItem(stack);
        return remainder.isEmpty() ? null : ItemStackTemplate.fromNonEmptyStack(remainder);
    }
}
