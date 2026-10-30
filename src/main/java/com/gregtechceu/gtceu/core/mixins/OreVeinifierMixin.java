package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Disables vanilla's large noise ore veins when GT's config asks for it. */
@Mixin(OreVeinRule.class)
public class OreVeinifierMixin {

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void gtceu$disableLargeVeins(MaterialRuleContext context, CallbackInfoReturnable<RuleEvaluator> cir) {
        if (ConfigHolder.INSTANCE != null && ConfigHolder.INSTANCE.worldgen.oreVeins.removeVanillaLargeOreVeins) {
            cir.setReturnValue((x, y, z) -> null);
        }
    }
}
