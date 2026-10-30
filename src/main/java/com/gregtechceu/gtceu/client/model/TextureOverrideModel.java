package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;

import com.gregtechceu.gtceu.compat.client.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import com.gregtechceu.gtceu.compat.client.model.BakedModelWrapper;
import com.gregtechceu.gtceu.compat.client.model.IQuadTransformer;
import com.gregtechceu.gtceu.compat.client.model.data.ModelData;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TextureOverrideModel<T extends BakedModel> extends BakedModelWrapper<T> {

    public static final IQuadTransformer OVERLAY_OFFSET = GTQuadTransformers.offset(0.002f);

    @Getter
    protected final Map<String, TextureAtlasSprite> textureOverrides;

    public TextureOverrideModel(T child, Map<String, TextureAtlasSprite> textureOverrides) {
        super(child);
        this.textureOverrides = textureOverrides;
    }

    public BakedModel getChild() {
        return this.originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData modelData, @Nullable ChunkSectionLayer renderType) {
        return retextureQuads(super.getQuads(state, side, rand, modelData, renderType), textureOverrides);
    }

    public static List<BakedQuad> retextureQuads(List<BakedQuad> quads, Map<String, TextureAtlasSprite> overrides) {
        List<BakedQuad> newQuads = new LinkedList<>();
        for (BakedQuad quad : quads) {
            String textureKey = quad.gtceu$getTextureKey();
            if (textureKey == null || textureKey.isEmpty()) continue;
            if (textureKey.charAt(0) == '#') {
                textureKey = textureKey.substring(1);
            }

            TextureAtlasSprite replacement = overrides.get(textureKey);
            if (replacement != null) {
                newQuads.add(GTQuadTransformers.setSprite(quad, replacement));
            } else {
                newQuads.add(quad);
            }
        }
        return newQuads;
    }
}
