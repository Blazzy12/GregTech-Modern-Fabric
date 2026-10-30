package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.platform.Transparency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Model faces may have UVs past the texture's edge (GT's pipe restrictor overlays do), which 1.21.1 allowed and
 * 26.x's translucency scan rejects. The scan is clamped to the texture.
 */
@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {

    @Inject(method = "computeTransparency(FFFF)Lcom/mojang/blaze3d/platform/Transparency;",
            at = @At("HEAD"), cancellable = true)
    private void gtceu$clampTransparencyRegion(float u0, float v0, float u1, float v1,
                                               CallbackInfoReturnable<Transparency> cir) {
        if (u0 < 0 || v0 < 0 || u1 > 1 || v1 > 1) {
            cir.setReturnValue(((SpriteContents) (Object) this).computeTransparency(
                    Mth.clamp(u0, 0, 1), Mth.clamp(v0, 0, 1), Mth.clamp(u1, 0, 1), Mth.clamp(v1, 0, 1)));
        }
    }
}
