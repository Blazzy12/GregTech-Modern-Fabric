package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.PowderSnowBlock;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Boots can let their wearer walk on powder snow (GT's nano and quantum boots), as NeoForge's item hook allows. */
@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {

    @ModifyReturnValue(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"))
    private static boolean gtceu$bootsWalkOnPowderSnow(boolean original, Entity entity) {
        if (original || !(entity instanceof LivingEntity living)) return original;
        var boots = living.getItemBySlot(EquipmentSlot.FEET);
        return !boots.isEmpty() && boots.getItem().canWalkOnPowderedSnow(boots, living);
    }
}
