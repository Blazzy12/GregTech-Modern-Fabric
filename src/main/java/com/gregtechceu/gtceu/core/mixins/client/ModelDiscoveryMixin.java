package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.compat.client.model.geometry.GeometryLoaderModel;

import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 26.x only loads the parents of models. Models with GregTech loaders refer to more (variant models, the parents of
 * inline models), which are loaded here. They are queued when the model is found, since that happens while the model
 * map is being written, and loaded before the parents are.
 */
@Mixin(ModelDiscovery.class)
public class ModelDiscoveryMixin {

    @Shadow
    @Final
    private ResolvableModel.Resolver resolver;

    @Unique
    private final Deque<GeometryLoaderModel> gtceu$pending = new ArrayDeque<>();

    @Inject(method = "createAndQueueWrapper", at = @At("RETURN"))
    private void gtceu$queueDependencies(Identifier id, UnbakedModel rawModel, CallbackInfoReturnable<?> cir) {
        if (rawModel instanceof GeometryLoaderModel model) {
            gtceu$pending.add(model);
        }
    }

    @Inject(method = "discoverDependencies", at = @At("HEAD"))
    private void gtceu$loadDependencies(List<?> toValidate, CallbackInfo ci) {
        GeometryLoaderModel model;
        while ((model = gtceu$pending.poll()) != null) {
            model.resolveDependencies(resolver);
        }
    }
}
