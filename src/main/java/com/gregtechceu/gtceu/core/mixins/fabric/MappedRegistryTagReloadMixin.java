package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.core.MappedRegistry;
import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Registry;

/** Keeps the contents of tags that loaded but are not applied yet, for GT's recipe generation. */
@Mixin(MappedRegistry.class)
public class MappedRegistryTagReloadMixin<T> {

    @Inject(method = "prepareTagReload", at = @At("HEAD"))
    private void gtceu$capturePendingTags(TagLoader.LoadResult<T> tags,
                                          CallbackInfoReturnable<Registry.PendingTags<T>> cir) {
        TagUtil.capturePendingTags(tags);
    }
}
