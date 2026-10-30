package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Adapts a GregTech {@link BakedModel} to a 26.x {@link BlockStateModel}.
 * <p>
 * In the world, the Fabric renderer calls {@link #emitQuads}, which builds the model data from the level and emits the
 * quads of every layer the model asks for, like NeoForge's chunk renderer did. Elsewhere (falling blocks, item
 * models, particles) the quads without model data are used, through {@link #collectParts}.
 */
public class GTBlockStateModel implements BlockStateModel {

    private final BakedModel model;
    private final @Nullable BlockState state;
    private @Nullable BlockStateModelPart staticPart;

    /**
     * @param state the block state the model is for, passed to the model when there is no level
     */
    public GTBlockStateModel(BakedModel model, @Nullable BlockState state) {
        this.model = model;
        this.state = state;
    }

    public BakedModel model() {
        return model;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        ModelData data = model.getModelData(level, pos, state, ModelData.EMPTY);
        long seed = random.nextLong();
        for (ChunkSectionLayer layer : model.getRenderTypes(state, random, data)) {
            for (Direction side : BakedModel.SIDES) {
                if (side != null && cullTest.test(side)) continue;
                random.setSeed(seed);
                for (BakedQuad quad : model.getQuads(state, side, random, data, layer)) {
                    quad.emit(emitter, layer, side);
                }
            }
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        output.add(staticPart(random));
    }

    private BlockStateModelPart staticPart(RandomSource random) {
        if (staticPart == null) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            long seed = random.nextLong();
            for (Direction side : BakedModel.SIDES) {
                random.setSeed(seed);
                for (BakedQuad quad : model.getQuads(state, side, random, ModelData.EMPTY, null)) {
                    if (side == null) {
                        builder.addUnculledFace(quad.toVanilla(null));
                    } else {
                        builder.addCulledFace(side, quad.toVanilla(null));
                    }
                }
            }
            staticPart = new SimpleModelWrapper(builder.build(), model.useAmbientOcclusion(), particleMaterial());
        }
        return staticPart;
    }

    @Override
    public Material.Baked particleMaterial() {
        return new Material.Baked(model.getParticleIcon(), false);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        ModelData data = model.getModelData(level, pos, state, ModelData.EMPTY);
        return new Material.Baked(model.getParticleIcon(data), false);
    }

    @Override
    public int materialFlags() {
        // translucent and animated quads may come and go with the model data, so assume both
        return net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_TRANSLUCENT |
                net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_ANIMATED;
    }
}
