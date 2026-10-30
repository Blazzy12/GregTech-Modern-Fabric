package com.gregtechceu.gtceu.core.mixins.fabric.events;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.entity.living.FinalizeSpawnEvent;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Posts the spawn finalisation event after vanilla equipped the mob. */
@Mixin(Mob.class)
public abstract class MobFinalizeSpawnMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void gtceu$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                     EntitySpawnReason reason, SpawnGroupData data,
                                     CallbackInfoReturnable<SpawnGroupData> cir) {
        NeoForge.EVENT_BUS.post(new FinalizeSpawnEvent((Mob) (Object) this, level, reason));
    }
}
