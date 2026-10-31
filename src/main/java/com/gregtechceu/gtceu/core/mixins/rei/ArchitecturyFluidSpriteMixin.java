package com.gregtechceu.gtceu.core.mixins.rei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.client.fluid.fabric.ClientFluidStackHooksImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * On Fabric, Architectury only knows the sprites of fluids registered through Architectury, and returns none for all
 * others (GT's included), so REI, which draws fluids through it, showed them as empty. Falls back to the fluid's baked
 * model, which is what vanilla and JEI use.
 */
@Mixin(value = ClientFluidStackHooksImpl.class, remap = false)
public class ArchitecturyFluidSpriteMixin {

    @ModifyReturnValue(method = "getSprite(Lnet/minecraft/world/level/material/Fluid;ZLdev/architectury/fluid/FluidStack;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                       at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private static @Nullable TextureAtlasSprite gtceu$modelSprite(@Nullable TextureAtlasSprite sprite, Fluid fluid,
                                                                  boolean still, FluidStack stack) {
        return sprite != null ? sprite : gtceu$fromModel(fluid, still);
    }

    @ModifyReturnValue(method = "getSprite(Lnet/minecraft/world/level/material/Fluid;ZLnet/minecraft/world/level/material/FluidState;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                       at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private static @Nullable TextureAtlasSprite gtceu$modelSpriteInWorld(@Nullable TextureAtlasSprite sprite,
                                                                         Fluid fluid, boolean still, FluidState state,
                                                                         BlockAndTintGetter level, BlockPos pos) {
        return sprite != null ? sprite : gtceu$fromModel(fluid, still);
    }

    @Unique
    private static @Nullable TextureAtlasSprite gtceu$fromModel(Fluid fluid, boolean still) {
        if (fluid.defaultFluidState().isEmpty()) return null;
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluid.defaultFluidState());
        return (still ? model.stillMaterial() : model.flowingMaterial()).sprite();
    }
}
