package com.gregtechceu.gtceu.client.model.item;

import java.util.Collection;

import com.gregtechceu.gtceu.compat.client.model.VanillaUnbakedModel;

import net.minecraft.client.resources.model.ModelBaker;

import net.minecraft.client.renderer.block.dispatch.ModelState;

import net.minecraft.client.resources.model.sprite.Material;

import com.gregtechceu.gtceu.compat.client.model.UnbakedModel;

import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;

import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryBakingContext;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryLoader;
import com.gregtechceu.gtceu.compat.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FacadeUnbakedModel implements IUnbakedGeometry<FacadeUnbakedModel> {

    private final @NotNull VanillaUnbakedModel defaultModel;

    private FacadeUnbakedModel(@NotNull VanillaUnbakedModel defaultModel) {
        this.defaultModel = defaultModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        BakedModel bakedParent = defaultModel.bake(baker, spriteGetter, modelState);
        return new FacadeCoverRenderer(bakedParent);
    }

    @Override
    public Collection<Identifier> getDependencies() {
        return defaultModel.getDependencies();
    }

    public static class Loader implements IGeometryLoader<FacadeUnbakedModel> {

        public static final Loader INSTANCE = new Loader();

        protected Loader() {}

        @Override
        public FacadeUnbakedModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
            return new FacadeUnbakedModel(VanillaUnbakedModel.parseInline(json.get("default_model")));
        }
    }
}
