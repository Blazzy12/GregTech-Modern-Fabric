package com.gregtechceu.gtceu.core.mixins.fabric;

import com.gregtechceu.gtceu.core.fabric.EntityCapabilityExt;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;

/** Makes the injected {@link EntityCapabilityExt} interface present at runtime. */
@Mixin(Entity.class)
public abstract class EntityCapabilityMixin implements EntityCapabilityExt {}
