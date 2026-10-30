package com.gregtechceu.gtceu.core.mixins.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;

import com.gregtechceu.gtceu.integration.recipeviewer.jei.JEIFluids;
import net.minecraft.client.renderer.RenderPipelines;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.library.render.FluidTankRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidTankRenderer.class, remap = false)
public class FluidTankRendererMixin {

    @Shadow
    @Final
    private int width;
    @Shadow
    @Final
    private int height;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/Object;II)V",
            at = @At("TAIL"),
            remap = false)
    private void gtceu$drawFreezingIndicator(GuiGraphicsExtractor graphics, Object ingredient, int x, int y, CallbackInfo ci) {
        if (!(ingredient instanceof IJeiFluidIngredient jeiFluid) || width < 6 || height < 6) return;
        FluidStack stack = JEIFluids.toGT(jeiFluid);
        if (stack.isEmpty()) return;
        var fluid = stack.getFluid();
        var material = ChemicalHelper.getMaterial(fluid);
        if (material == null || !material.requiresMetalFreezing() ||
                !fluid.isSame(material.getFluid(FluidStorageKeys.LIQUID)))
            return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + width - 5, y);
        graphics.pose().scale(0.75f, 0.75f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GTCEu.id("textures/gui/fluid_overlay/flame.png"), 0, 0, 0, 0,
                8, 8, 8, 8);
        graphics.pose().popMatrix();
    }
}
