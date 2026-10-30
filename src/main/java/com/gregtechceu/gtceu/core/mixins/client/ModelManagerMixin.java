package com.gregtechceu.gtceu.core.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.client.util.ModelEventHelper;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import com.gregtechceu.gtceu.compat.fml.ModLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ModelManager.class)
public abstract class ModelManagerMixin {

    @Inject(method = "reload", at = @At(value = "HEAD"))
    private void gtceu$loadDynamicModels(PreparableReloadListener.SharedState currentReload, Executor taskExecutor,
                                         PreparableReloadListener.PreparationBarrier preparationBarrier,
                                         Executor reloadExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ModelEventHelper.onReload();
        if (ModLoader.hasErrors()) {
            GTCEu.LOGGER.warn("GregTech Model loading CANCELLED because loading errors have been encountered");
            return;
        }

        long startTime = System.currentTimeMillis();
        // turns out these do have to be init in here after all, as they check for asset existence. whoops.
        ModLoader.postEventWrapContainerInModOrder(new RegisterDynamicResourcesEvent());
        com.gregtechceu.gtceu.client.model.item.ItemDefinitions.generateMissing(currentReload.resourceManager());

        GTCEu.LOGGER.info("GregTech Model loading took {}ms", System.currentTimeMillis() - startTime);
    }

    @Inject(method = "apply", at = @At("TAIL"))
    private void gtceu$onModelsApplied(CallbackInfo ci) {
        ModelEventHelper.onModelsApplied();
    }
}
