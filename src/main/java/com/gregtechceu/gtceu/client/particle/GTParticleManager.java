package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;
import com.gregtechceu.gtceu.compat.client.render.RecordingBufferSource;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.bloom.EffectRenderContext;
import com.gregtechceu.gtceu.client.bloom.IRenderSetup;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Singleton class responsible for managing, updating and rendering {@link GTParticle} instances.
 */
public final class GTParticleManager {

    public static final GTParticleManager INSTANCE = new GTParticleManager();

    private final Map<@Nullable IRenderSetup, Queue<GTParticle>> depthEnabledParticles = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<@Nullable IRenderSetup, Queue<GTParticle>> depthDisabledParticles = new Object2ObjectLinkedOpenHashMap<>();

    private final List<GTParticle> newParticleQueue = new ArrayList<>();

    private GTParticleManager() {}

    public void addEffect(GTParticle particles) {
        newParticleQueue.add(particles);
    }

    public void updateEffects() {
        if (!this.depthEnabledParticles.isEmpty()) {
            updateQueue(this.depthEnabledParticles);
        }
        if (!this.depthDisabledParticles.isEmpty()) {
            updateQueue(this.depthDisabledParticles);
        }

        if (!newParticleQueue.isEmpty()) {
            for (GTParticle particle : newParticleQueue) {
                var queue = particle.shouldDisableDepth() ? this.depthDisabledParticles : this.depthEnabledParticles;

                Queue<GTParticle> particles = queue.computeIfAbsent(particle.getRenderSetup(),
                        setup -> new ArrayDeque<>());

                if (particles.size() > 6000) {
                    particles.remove().setExpired();
                }
                particles.add(particle);
            }

            newParticleQueue.clear();
        }
    }

    private void updateQueue(Map<@Nullable IRenderSetup, Queue<GTParticle>> renderQueue) {
        Iterator<Queue<GTParticle>> it = renderQueue.values().iterator();
        while (it.hasNext()) {
            Queue<GTParticle> particlesForSetup = it.next();

            Iterator<GTParticle> particles = particlesForSetup.iterator();
            while (particles.hasNext()) {
                GTParticle particle = particles.next();

                if (particle.isAlive()) {
                    try {
                        particle.onUpdate();
                    } catch (RuntimeException exception) {
                        GTCEu.LOGGER.error("Particle update error: {}", particle, exception);
                        particle.setExpired();
                    }
                    if (particle.isAlive()) continue;
                }

                particles.remove();
            }

            if (particlesForSetup.isEmpty()) {
                it.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (GTParticle particle : this.newParticleQueue) {
                particle.setExpired();
            }
            this.newParticleQueue.clear();
        }
        for (Queue<GTParticle> particles : this.depthEnabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        for (Queue<GTParticle> particles : this.depthDisabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        this.depthEnabledParticles.clear();
        this.depthDisabledParticles.clear();
    }

    /** Registers the tick and render hooks. */
    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(INSTANCE::renderParticles);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.isPaused() && client.level != null) {
                INSTANCE.updateEffects();
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> INSTANCE.clearAllEffects(true));
    }

    private final RecordingBufferSource buffers = new RecordingBufferSource();

    private void renderParticles(LevelRenderContext event) {
        if (this.depthEnabledParticles.isEmpty() && this.depthDisabledParticles.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        EffectRenderContext instance = EffectRenderContext.getInstance()
                .update(camera, event.levelState().cameraRenderState.cullFrustum,
                        mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));

        PoseStack poseStack = new PoseStack();
        poseStack.translate(-camera.position().x, -camera.position().y, -camera.position().z);

        buffers.clear();
        // depth-disabled particles used to be drawn without writing depth; translucent render types don't either
        renderParticlesInLayer(poseStack, this.depthDisabledParticles, instance, buffers);
        renderParticlesInLayer(poseStack, this.depthEnabledParticles, instance, buffers);
        buffers.submit(event.poseStack(), event.submitNodeCollector());
    }

    private static void renderParticlesInLayer(PoseStack poseStack,
                                               Map<@Nullable IRenderSetup, Queue<GTParticle>> renderQueue,
                                               EffectRenderContext context, MultiBufferSource buffers) {
        for (var entry : renderQueue.entrySet()) {
            Queue<GTParticle> particles = entry.getValue();
            if (particles.isEmpty()) continue;

            IRenderSetup handler = entry.getKey();
            VertexConsumer buffer = null;

            for (GTParticle particle : particles) {
                if (!particle.shouldRender(context)) {
                    continue;
                }
                try {
                    if (buffer == null) {
                        buffer = buffers.getBuffer(handler != null ? handler.getRenderType() :
                                RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_PARTICLES));
                    }
                    particle.renderParticle(poseStack, buffer, context);
                } catch (Throwable throwable) {
                    GTCEu.LOGGER.error("Particle render error: {}", particle, throwable);
                    particle.setExpired();
                }
            }
        }
    }

    private static int count(Map<@Nullable IRenderSetup, Queue<GTParticle>> renderQueue) {
        return renderQueue.values().stream().mapToInt(Queue::size).sum();
    }
}
