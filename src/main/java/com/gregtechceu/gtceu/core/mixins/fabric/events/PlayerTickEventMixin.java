package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.entity.player.PlayerEvent;
import com.gregtechceu.gtceu.compat.event.tick.PlayerTickEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/** Posts player tick and break speed events. */
@Mixin(Player.class)
public abstract class PlayerTickEventMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void gtceu$preTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre((Player) (Object) this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gtceu$postTick(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post((Player) (Object) this));
    }

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float gtceu$breakSpeed(float speed, BlockState state) {
        var event = NeoForge.EVENT_BUS.post(new PlayerEvent.BreakSpeed((Player) (Object) this, state, speed, speed,
                Optional.empty()));
        return event.isCanceled() ? 0 : event.getNewSpeed();
    }
}
