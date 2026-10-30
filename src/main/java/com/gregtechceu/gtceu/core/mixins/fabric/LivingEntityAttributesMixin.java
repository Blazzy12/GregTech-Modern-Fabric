package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.compat.common.NeoForgeMod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Adds and applies the attributes NeoForge gives every living entity ({@link NeoForgeMod}). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttributesMixin {

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder gtceu$addAttributes(AttributeSupplier.Builder builder) {
        return builder.add(NeoForgeMod.SWIM_SPEED).add(NeoForgeMod.CREATIVE_FLIGHT);
    }

    @ModifyArg(method = "travelInWater",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
               index = 0)
    private float gtceu$applySwimSpeed(float speed) {
        return speed * (float) ((LivingEntity) (Object) this).getAttributeValue(NeoForgeMod.SWIM_SPEED);
    }
}
