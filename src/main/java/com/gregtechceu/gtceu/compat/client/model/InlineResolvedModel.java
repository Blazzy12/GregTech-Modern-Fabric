package com.gregtechceu.gtceu.compat.client.model;

import net.minecraft.client.resources.model.ResolvedModel;

import org.jetbrains.annotations.Nullable;

/** A model defined inside another model's JSON, resolved against its parent at bake time. */
public record InlineResolvedModel(net.minecraft.client.resources.model.UnbakedModel wrapped,
                                  @Nullable ResolvedModel parent, String debugName)
        implements ResolvedModel {}
