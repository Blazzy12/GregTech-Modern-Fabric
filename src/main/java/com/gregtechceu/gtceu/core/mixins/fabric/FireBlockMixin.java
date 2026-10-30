package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.IIgnitableBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lets {@link IIgnitableBlock}s catch fire from spreading fire, like vanilla does for TNT. */
@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Inject(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private void gtceu$igniteBlock(Level level, BlockPos pos, int chance, RandomSource random, int age,
                                   CallbackInfo ci, @Local(ordinal = 0) BlockState oldState) {
        if (oldState.getBlock() instanceof IIgnitableBlock ignitable) {
            ignitable.onCaughtFire(oldState, level, pos, null, null);
        }
    }
}
