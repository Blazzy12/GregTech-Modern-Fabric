package com.gregtechceu.gtceu.core.mixins.jei;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.JEIFluids;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.fabric.platform.FluidHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/** Adds GT's fluid tooltips (temperature, state, hazards) to fluids shown in JEI. */
@Mixin(value = FluidHelper.class, remap = false)
public class FluidHelperMixin {

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Component> gtceu$injectFluidTooltips(List<Component> tooltip, IJeiFluidIngredient ingredient,
                                                      Player player, Item.TooltipContext context,
                                                      TooltipFlag tooltipFlag) {
        List<Component> lines = new ArrayList<>(tooltip);
        TooltipsHandler.appendFluidTooltips(JEIFluids.toGT(ingredient), lines::add, tooltipFlag, context);
        return lines;
    }
}
