package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import com.gregtechceu.gtceu.compat.client.model.IDynamicBakedModel;

public abstract class BaseBakedModel implements IDynamicBakedModel {

    public BaseBakedModel() {}

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return BakedModel.missingSprite();
    }
}
