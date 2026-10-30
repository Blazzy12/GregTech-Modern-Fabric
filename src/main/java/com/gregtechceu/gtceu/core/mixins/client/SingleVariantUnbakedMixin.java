package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.compat.client.model.GTBlockStateModel;
import com.gregtechceu.gtceu.compat.client.model.geometry.GeometryLoaderModel;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Blockstate variants that use a model with a GregTech loader bake to its dynamic model. */
@Mixin(SingleVariant.Unbaked.class)
public class SingleVariantUnbakedMixin {

    @Shadow
    @Final
    private Variant variant;

    @Inject(method = "bake", at = @At("HEAD"), cancellable = true)
    private void gtceu$bakeDynamicModel(ModelBaker baker, CallbackInfoReturnable<BlockStateModel> cir) {
        ResolvedModel model = baker.getModel(variant.modelLocation());
        if (model.wrapped() instanceof GeometryLoaderModel custom) {
            cir.setReturnValue(new GTBlockStateModel(
                    custom.bake(model, baker, variant.modelState().asModelState()), null));
        }
    }
}
