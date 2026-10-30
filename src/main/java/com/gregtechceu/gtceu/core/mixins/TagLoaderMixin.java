package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

/**
 * Adds GT's generated tags (material items, blocks, fluids) to tags as they load. 26.x has no TagManager to tell the
 * loader its registry, so it is found from the loader's directory.
 */
@Mixin(value = TagLoader.class, priority = 500)
public class TagLoaderMixin {

    @Shadow
    @Final
    private String directory;

    /**
     * 26.x loads tags before the rest of the data (and so before GT's recipe generation, which used to fill the
     * material data first), so fill it here, for the generated tags.
     */
    @Inject(method = "loadTagsForExistingRegistries", at = @At("HEAD"))
    private static void gtceu$initMaterialData(ResourceManager manager, RegistryAccess layer,
                                               CallbackInfoReturnable<List<Registry.PendingTags<?>>> cir) {
        ItemMaterialData.reinitializeMaterialData();
    }

    @Inject(method = "load", at = @At(value = "RETURN"))
    public void gtceu$load(ResourceManager resourceManager,
                           CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir) {
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (Registries.tagsDirPath(registry.key()).equals(directory)) {
                MixinHelpers.generateGTDynamicTags(cir.getReturnValue(), registry);
                return;
            }
        }
    }
}
