package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.LevelCapabilityExt;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;

/** Makes the injected {@link LevelCapabilityExt} interface present at runtime. */
@Mixin(Level.class)
public abstract class LevelCapabilityMixin implements LevelCapabilityExt {}
