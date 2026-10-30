package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

public record PatternErrorType(Identifier id, MapCodec<? extends PatternError> codec) {}
