package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.ClientEventListener;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.PlayerSkin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    /** GregTech capes replace the player's own. */
    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin gtceu$applyCape(PlayerSkin skin) {
        Identifier cape = CapeRegistry.getPlayerCapeTexture(((AbstractClientPlayer) (Object) this).getUUID());
        if (cape == null) return skin;
        var texture = new ClientAsset.ResourceTexture(cape, cape);
        return new PlayerSkin(skin.body(), texture, skin.elytra(), skin.model(), skin.secure());
    }

    /** GregTech's speed boosts don't widen the field of view. */
    @WrapOperation(method = "getFieldOfViewModifier",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/player/AbstractClientPlayer;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double gtceu$fovMovementSpeed(AbstractClientPlayer player, Holder<Attribute> attribute,
                                          Operation<Double> original) {
        if (attribute == Attributes.MOVEMENT_SPEED) {
            AttributeInstance instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) return ClientEventListener.getFovMovementSpeed(instance);
        }
        return original.call(player, attribute);
    }
}
