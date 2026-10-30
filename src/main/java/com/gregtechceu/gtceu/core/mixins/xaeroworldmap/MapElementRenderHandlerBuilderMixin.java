package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore.OreVeinElementRenderer;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.element.MapElementRenderHandler;
import xaero.map.element.render.ElementRenderer;

import java.util.List;

/** Adds GT's ore vein markers to the world map's element renderers. */
@Mixin(value = MapElementRenderHandler.Builder.class, remap = false)
public class MapElementRenderHandlerBuilderMixin {

    @Inject(method = "build",
            at = @At(value = "INVOKE", target = "Lxaero/map/mods/SupportMods;pac()Z"))
    private void gtceu$addOreRenderer(CallbackInfoReturnable<MapElementRenderHandler> cir,
                                      @Local List<ElementRenderer<?, ?, ?>> renderers) {
        if (ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) {
            renderers.add(OreVeinElementRenderer.Builder.begin().build());
        }
    }
}
