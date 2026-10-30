package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** A model of fixed quads, like the baked form of a JSON model. */
public class SimpleBakedModel implements BakedModel {

    private final Map<Direction, List<BakedQuad>> culledQuads;
    private final List<BakedQuad> unculledQuads;
    private final boolean ambientOcclusion;
    private final TextureAtlasSprite particle;
    private final ItemTransforms transforms;
    private final ChunkRenderTypeSet renderTypes;

    public SimpleBakedModel(Map<Direction, List<BakedQuad>> culledQuads, List<BakedQuad> unculledQuads,
                            boolean ambientOcclusion, TextureAtlasSprite particle, ItemTransforms transforms) {
        this.culledQuads = culledQuads;
        this.unculledQuads = unculledQuads;
        this.ambientOcclusion = ambientOcclusion;
        this.particle = particle;
        this.transforms = transforms;
        List<ChunkSectionLayer> layers = new ArrayList<>();
        for (List<BakedQuad> quads : culledQuads.values()) quads.forEach(q -> addLayer(layers, q));
        unculledQuads.forEach(q -> addLayer(layers, q));
        this.renderTypes = layers.isEmpty() ? ChunkRenderTypeSet.of(ChunkSectionLayer.SOLID) :
                ChunkRenderTypeSet.of(layers);
    }

    private static void addLayer(List<ChunkSectionLayer> layers, BakedQuad quad) {
        if (!layers.contains(quad.getLayer())) layers.add(quad.getLayer());
    }

    /** Converts vanilla quads, keeping the texture keys and face layers recorded while they were baked. */
    public static SimpleBakedModel of(QuadCollection quads,
                                      Map<net.minecraft.client.resources.model.geometry.BakedQuad, net.minecraft.client.resources.model.cuboid.CuboidFace> faces,
                                      boolean ambientOcclusion, TextureAtlasSprite particle,
                                      ItemTransforms transforms) {
        Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            List<BakedQuad> converted = new ArrayList<>();
            for (var quad : quads.getQuads(side)) converted.add(QuadMetadata.convert(quad, faces));
            culled.put(side, converted);
        }
        List<BakedQuad> unculled = new ArrayList<>();
        for (var quad : quads.getQuads(null)) unculled.add(QuadMetadata.convert(quad, faces));
        return new SimpleBakedModel(culled, unculled, ambientOcclusion, particle, transforms);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable ChunkSectionLayer renderType) {
        List<BakedQuad> quads = side == null ? unculledQuads : culledQuads.getOrDefault(side, List.of());
        if (renderType == null) return new ArrayList<>(quads);
        List<BakedQuad> filtered = new ArrayList<>();
        for (BakedQuad quad : quads) {
            if (quad.getLayer() == renderType) filtered.add(quad);
        }
        return filtered;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return renderTypes;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }
}
