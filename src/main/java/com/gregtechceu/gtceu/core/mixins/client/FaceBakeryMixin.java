package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.compat.client.model.QuadMetadata;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Remembers which model face a quad came from, for GregTech's texture keys and face layers. */
@Mixin(value = FaceBakery.class, priority = 1500)
public class FaceBakeryMixin {

    @ModifyReturnValue(method = "bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace;Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;Lnet/minecraft/core/Direction;I)Lnet/minecraft/client/resources/model/geometry/BakedQuad;",
                       at = @At("RETURN"))
    private static BakedQuad gtceu$rememberFace(BakedQuad quad, @Local(argsOnly = true) CuboidFace face) {
        QuadMetadata.onFaceBaked(quad, face);
        return quad;
    }
}
