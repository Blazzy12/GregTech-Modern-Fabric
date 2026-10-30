package com.gregtechceu.gtceu.compat.client.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/** A pre-26.x item model property, read by the {@code gtceu:legacy_property} range dispatch property. */
@FunctionalInterface
public interface ItemPropertyFunction {

    float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
}
