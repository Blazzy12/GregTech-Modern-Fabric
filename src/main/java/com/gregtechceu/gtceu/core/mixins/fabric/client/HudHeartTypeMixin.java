package com.gregtechceu.gtceu.core.mixins.fabric.client;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.entity.player.PlayerHeartTypeEvent;

import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fires NeoForge's {@code PlayerHeartTypeEvent}, which GT uses to show poisoned hearts for weak poison. */
@Mixin(Hud.HeartType.class)
public abstract class HudHeartTypeMixin {

    @ModifyReturnValue(method = "forPlayer", at = @At("RETURN"))
    private static Hud.HeartType gtceu$postHeartTypeEvent(Hud.HeartType original, Player player) {
        return NeoForge.EVENT_BUS.post(new PlayerHeartTypeEvent(player, original, original)).getType();
    }
}
