package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

/**
 * GregTech's render types on the 26.x pipeline API.
 * <p>
 * Bloom used its own shader and render target. Until that post effect is ported, "bloom" geometry renders through
 * vanilla's translucent and emissive types, so it still glows (full-bright) without the blur.
 */
public final class GTRenderTypes {

    private static final RenderPipeline LIGHT_RING_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(GTCEu.id("pipeline/light_ring"))
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
                    .withCull(false)
                    .build());

    private static final RenderType LIGHT_RING = RenderType.create("gtceu:light_ring",
            RenderSetup.builder(LIGHT_RING_PIPELINE).createRenderSetup());

    private GTRenderTypes() {}

    public static RenderType lightRing() {
        return LIGHT_RING;
    }

    /** Block-format geometry that glows. */
    public static RenderType bloom() {
        return RenderTypes.translucentMovingBlock();
    }

    /** Block-format overlay geometry drawn over a block's faces. */
    public static RenderType faceLayer() {
        return RenderTypes.cutoutMovingBlock();
    }

    public static RenderType entityBloom(Identifier location) {
        return RenderTypes.entityTranslucentEmissive(location);
    }

    public static RenderType entityBloomBlockSheet() {
        return entityBloom(TextureAtlas.LOCATION_BLOCKS);
    }

    public static RenderType assemblyLine() {
        return RenderTypes.lightning();
    }

    public static RenderType getMonitor() {
        return RenderTypes.debugQuads();
    }
}
