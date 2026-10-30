package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.core.fabric.DropCapture;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** While a living entity drops its death loot, the item entities are collected instead of spawned. */
@Mixin(Entity.class)
public abstract class EntityDropCaptureMixin {

    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean gtceu$captureDrop(ServerLevel level, Entity entity, Operation<Boolean> original) {
        if (entity instanceof ItemEntity item && DropCapture.capture(item)) return true;
        return original.call(level, entity);
    }
}
