package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.compat.common.NeoForgeMod;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@link NeoForgeMod#CREATIVE_FLIGHT}. Only flight that this granted is taken away again, so other sources of
 * flight (game mode, other mods) are left alone.
 */
@Mixin(Player.class)
public abstract class PlayerCreativeFlightMixin {

    private static final String GRANTED = "gtceu:granted_flight";

    @Inject(method = "tick", at = @At("TAIL"))
    private void gtceu$updateCreativeFlight(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide()) return;
        var abilities = player.getAbilities();
        var data = player.getPersistentData();
        boolean attribute = player.getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT) > 0;
        if (attribute && !abilities.mayfly) {
            abilities.mayfly = true;
            data.putBoolean(GRANTED, true);
            player.onUpdateAbilities();
        } else if (!attribute && data.getBooleanOr(GRANTED, false)) {
            data.remove(GRANTED);
            if (!player.isCreative() && !player.isSpectator()) {
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
