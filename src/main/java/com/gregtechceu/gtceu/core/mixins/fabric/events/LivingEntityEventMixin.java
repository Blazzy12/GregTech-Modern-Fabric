package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.EventHooks;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingDropsEvent;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingEvent;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingFallEvent;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingIncomingDamageEvent;
import com.gregtechceu.gtceu.compat.event.tick.EntityTickEvent;
import com.gregtechceu.gtceu.core.fabric.DropCapture;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Posts the living entity events GregTech listens to. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEventMixin {

    @Shadow
    public abstract int getUseItemRemainingTicks();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gtceu$preTick(CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre((LivingEntity) (Object) this)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gtceu$postTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post((LivingEntity) (Object) this));
    }

    @WrapMethod(method = "causeFallDamage")
    private boolean gtceu$fall(double distance, float multiplier, DamageSource source, Operation<Boolean> original) {
        var event = NeoForge.EVENT_BUS.post(new LivingFallEvent((LivingEntity) (Object) this, distance, multiplier));
        if (event.isCanceled()) return false;
        return original.call(event.getDistance(), event.getDamageMultiplier(), source);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void gtceu$jump(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent((LivingEntity) (Object) this));
    }

    @WrapMethod(method = "hurtServer")
    private boolean gtceu$incomingDamage(ServerLevel level, DamageSource source, float amount,
                                         Operation<Boolean> original) {
        var event = NeoForge.EVENT_BUS.post(new LivingIncomingDamageEvent((LivingEntity) (Object) this, source,
                amount));
        if (event.isCanceled()) return false;
        return original.call(level, source, event.getAmount());
    }

    /** Collects the drops, so listeners can take them before they are spawned. */
    @WrapMethod(method = "dropAllDeathLoot")
    private void gtceu$drops(ServerLevel level, DamageSource source, Operation<Void> original) {
        List<ItemEntity> drops = DropCapture.start();
        try {
            original.call(level, source);
        } finally {
            DropCapture.stop();
        }
        LivingEntity self = (LivingEntity) (Object) this;
        var event = NeoForge.EVENT_BUS.post(new LivingDropsEvent(self, source, drops,
                self.getLastHurtByPlayer() != null));
        if (!event.isCanceled()) {
            for (ItemEntity drop : event.getDrops()) {
                level.addFreshEntity(drop);
            }
        }
    }

    @WrapOperation(method = "completeUsingItem",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack gtceu$useItemFinish(ItemStack stack, Level level, LivingEntity entity,
                                          Operation<ItemStack> original) {
        ItemStack copy = stack.copy();
        ItemStack result = original.call(stack, level, entity);
        return EventHooks.onItemUseFinish(entity, copy, getUseItemRemainingTicks(), result);
    }
}
