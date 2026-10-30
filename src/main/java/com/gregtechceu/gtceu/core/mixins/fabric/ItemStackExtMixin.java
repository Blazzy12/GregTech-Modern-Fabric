package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.ItemStackExt;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;

/** Makes the injected {@link ItemStackExt} interface present at runtime. */
@Mixin(ItemStack.class)
public abstract class ItemStackExtMixin implements ItemStackExt {}
