package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * GregTech's baked model API, shaped like the NeoForge one its models were written against. 26.x block models are
 * {@code BlockStateModel}s; {@link GTBlockStateModel} adapts these models to it, asking for the quads of every layer in
 * {@link #getRenderTypes} and emitting them through the Fabric renderer.
 */
public interface BakedModel {

    /**
     * @param renderType the layer to get the quads of, or {@code null} for all layers
     */
    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                             ModelData data, @Nullable ChunkSectionLayer renderType);

    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    /** @return the layers this model has quads in */
    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        List<ChunkSectionLayer> layers = new ArrayList<>();
        for (Direction side : SIDES) {
            for (BakedQuad quad : getQuads(state, side, rand, data, null)) {
                if (!layers.contains(quad.getLayer())) layers.add(quad.getLayer());
            }
        }
        return layers.isEmpty() ? ChunkRenderTypeSet.of(ChunkSectionLayer.SOLID) : ChunkRenderTypeSet.of(layers);
    }

    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        return data;
    }

    default boolean useAmbientOcclusion() {
        return true;
    }

    default boolean isGui3d() {
        return true;
    }

    default boolean usesBlockLight() {
        return true;
    }

    default boolean isCustomRenderer() {
        return false;
    }

    default TextureAtlasSprite getParticleIcon() {
        return missingSprite();
    }

    default TextureAtlasSprite getParticleIcon(ModelData data) {
        return getParticleIcon();
    }

    default ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    /** Every face direction, then {@code null} for unculled quads. */
    Direction[] SIDES = { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, null };

    static TextureAtlasSprite missingSprite() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }
}
