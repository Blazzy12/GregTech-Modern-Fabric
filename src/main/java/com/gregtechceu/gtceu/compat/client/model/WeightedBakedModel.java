package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Picks one of several models by weight, per position. */
public class WeightedBakedModel implements BakedModel {

    private final WeightedList<BakedModel> models;
    private final BakedModel first;

    private WeightedBakedModel(WeightedList<BakedModel> models, BakedModel first) {
        this.models = models;
        this.first = first;
    }

    private BakedModel pick(RandomSource rand) {
        return models.getRandomOrThrow(rand);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable ChunkSectionLayer renderType) {
        return pick(rand).getQuads(state, side, rand, data, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        List<ChunkRenderTypeSet> sets = new ArrayList<>();
        for (Weighted<BakedModel> model : models.unwrap()) {
            sets.add(model.value().getRenderTypes(state, rand, data));
        }
        return ChunkRenderTypeSet.union(sets);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        return first.getModelData(level, pos, state, data);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return first.useAmbientOcclusion();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return first.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return first.getParticleIcon(data);
    }

    @Override
    public ItemTransforms getTransforms() {
        return first.getTransforms();
    }

    public static class Builder {

        private final WeightedList.Builder<BakedModel> builder = WeightedList.builder();
        private @Nullable BakedModel first;
        private int count;

        public Builder add(@Nullable BakedModel model, int weight) {
            if (model == null) return this;
            if (first == null) first = model;
            builder.add(model, weight);
            count++;
            return this;
        }

        public @Nullable BakedModel build() {
            if (first == null) return null;
            if (count == 1) return first;
            return new WeightedBakedModel(builder.build(), first);
        }
    }
}
