package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.ItemExt;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;

/** Makes the injected {@link ItemExt} interface present at runtime. */
@Mixin(Item.class)
public abstract class ItemExtMixin implements ItemExt {}
