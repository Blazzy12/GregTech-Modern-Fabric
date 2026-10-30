package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A vanilla block model seen as a GregTech {@link BakedModel}, so GregTech models can wrap it. */
public class VanillaBakedModel implements BakedModel {

    private final BlockStateModel model;
    private final Map<BlockStateModelPart, PartQuads> converted = new ConcurrentHashMap<>();

    public VanillaBakedModel(BlockStateModel model) {
        this.model = model;
    }

    public BlockStateModel vanillaModel() {
        return model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable ChunkSectionLayer renderType) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(rand, parts);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : converted.computeIfAbsent(part, PartQuads::new).get(side)) {
                if (renderType == null || quad.getLayer() == renderType) quads.add(quad);
            }
        }
        return quads;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return model.particleMaterial().sprite();
    }

    private static final class PartQuads {

        private final Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        private final List<BakedQuad> unculled = new ArrayList<>();

        PartQuads(BlockStateModelPart part) {
            for (Direction side : Direction.values()) {
                List<BakedQuad> quads = new ArrayList<>();
                for (var quad : part.getQuads(side)) quads.add(BakedQuad.of(quad));
                culled.put(side, quads);
            }
            for (var quad : part.getQuads(null)) unculled.add(BakedQuad.of(quad));
        }

        List<BakedQuad> get(@Nullable Direction side) {
            return side == null ? unculled : culled.get(side);
        }
    }
}
