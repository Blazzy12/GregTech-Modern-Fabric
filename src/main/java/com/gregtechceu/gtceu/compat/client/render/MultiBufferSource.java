package com.gregtechceu.gtceu.compat.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * The vertex buffer source renderers used to draw into before 26.x, where rendering is submitted to a
 * {@code SubmitNodeCollector}. GregTech's renderers still draw into one; {@link RecordingBufferSource} records what they
 * draw and submits it.
 */
@FunctionalInterface
public interface MultiBufferSource {

    VertexConsumer getBuffer(RenderType renderType);
}
