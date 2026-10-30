package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.compat.client.render.RecordingBufferSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Draws the registered bloom effects into the world each frame. */
public final class BloomRenderer {

    static final ReadWriteLock BLOOM_RENDER_LOCK = new ReentrantReadWriteLock();
    /** The render type of effects registered without a render setup: particle-format quads on the block atlas. */
    static final RenderType DEFAULT_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);

    private static final RecordingBufferSource BUFFERS = new RecordingBufferSource();

    private BloomRenderer() {}

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(BloomRenderer::collectSubmits);
    }

    private static void collectSubmits(LevelRenderContext context) {
        if (!BloomShaderManager.isBloomActive()) return;

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BloomHandler.initializeScheduledRenders();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
        if (BloomHandler.BLOOM_RENDERS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        EffectRenderContext effectContext = EffectRenderContext.getInstance()
                .update(mc.gameRenderer.mainCamera(), context.levelState().cameraRenderState.cullFrustum,
                        mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));

        BUFFERS.clear();
        PoseStack poseStack = new PoseStack();
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            BloomHandler.BLOOM_RENDERS.forEach((renderSetup, list) -> list.draw(poseStack, effectContext, BUFFERS));
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }
        BUFFERS.submit(context.poseStack(), context.submitNodeCollector());

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BloomHandler.removeInvalidatedRenders();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }
}
