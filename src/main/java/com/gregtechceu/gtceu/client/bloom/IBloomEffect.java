package com.gregtechceu.gtceu.client.bloom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Render callback interface for {@link BloomHandler#registerBloomRender(IRenderSetup, IBloomEffect, BlockEntity)}.
 */
@FunctionalInterface
public interface IBloomEffect {

    /**
     * Render the bloom effect.
     *
     * @param buffer  buffer builder
     * @param context render context
     */
    @Environment(EnvType.CLIENT)
    void renderBloomEffect(PoseStack poseStack, VertexConsumer buffer, EffectRenderContext context);

    /**
     * @param context render context
     * @return if this effect should be rendered; returning {@code false} skips calling
     *         {@link #renderBloomEffect(PoseStack, VertexConsumer, EffectRenderContext)}.
     */
    @Environment(EnvType.CLIENT)
    default boolean shouldRenderBloomEffect(EffectRenderContext context) {
        return true;
    }
}
