package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.client.player.LocalPlayer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    /**
     * With armor that allows it, moving in any direction counts as moving forward for starting and keeping a sprint.
     */
    @ModifyExpressionValue(method = { "canStartSprinting", "aiStep", "shouldStopRunSprinting" },
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/player/ClientInput;hasForwardImpulse()Z"),
                           require = 0)
    private boolean gtceu$omnidirectionalSprint(boolean original) {
        if (original) return true;
        LocalPlayer self = (LocalPlayer) (Object) this;
        return self.input.getMoveVector().length() > 1.0E-5F && ArmorUtils.canSprintInAnyDirection(self);
    }
}
