package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.fabric.EarlyComponentBinder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The early bind runs with vanilla's data registries only, so an element whose components point at modded data
 * (a jukebox song, say) is left with what it had so far instead of failing every element.
 */
@Mixin(DataComponentInitializers.class)
public class DataComponentInitializersMixin {

    @WrapOperation(method = "runInitializers",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/core/component/DataComponentInitializers$InitializerEntry;run(Lnet/minecraft/core/component/DataComponentMap$Builder;Lnet/minecraft/core/HolderLookup$Provider;)V"))
    private void gtceu$skipFailingEarlyInitializers(DataComponentInitializers.InitializerEntry<?> entry,
                                                    DataComponentMap.Builder components,
                                                    HolderLookup.Provider context, Operation<Void> original) {
        if (!EarlyComponentBinder.isBinding()) {
            original.call(entry, components, context);
            return;
        }
        try {
            original.call(entry, components, context);
        } catch (RuntimeException e) {
            GTCEu.LOGGER.debug("Skipped early default components of {}: {}", entry.key(), e.getMessage());
        }
    }
}
