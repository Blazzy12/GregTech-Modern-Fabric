package com.gregtechceu.gtceu.client.model.item;

import com.gregtechceu.gtceu.compat.client.model.VanillaUnbakedModel;

import net.minecraft.client.resources.model.ModelBaker;

import net.minecraft.client.renderer.block.dispatch.ModelState;

import net.minecraft.client.resources.model.sprite.Material;

import com.gregtechceu.gtceu.compat.client.model.UnbakedModel;

import com.gregtechceu.gtceu.GTCEu;

import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import com.gregtechceu.gtceu.compat.client.model.BakedModelWrapper;
import com.gregtechceu.gtceu.compat.client.model.generators.CustomLoaderBuilder;
import com.gregtechceu.gtceu.compat.client.model.generators.ModelBuilder;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryBakingContext;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryLoader;
import com.gregtechceu.gtceu.compat.client.model.geometry.IUnbakedGeometry;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Function;

// spotless:off
/**
 * Custom model type that simply overrides {@link BakedModel#isCustomRenderer()} to always return true so
 * {@link IClientItemExtensions#getCustomRenderer()} can work. Does nothing on blocks.
 *
 * <p>
 * Use it on an item model builder like this:
 * <pre>{@code
 * prov.getBuilder(ctx.getId())
 *         .customLoader(CustomItemRendererModel.Builder::begin).end()
 *         // ...
 * }</pre>
 */
// spotless:on
public class CustomItemRendererWrapperModel implements IUnbakedGeometry<CustomItemRendererWrapperModel> {

    public static final Identifier ID = GTCEu.id("custom_item_renderer_wrapper");

    public CustomItemRendererWrapperModel() {}

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides itemOverrides) {
        return new Baked(VanillaUnbakedModel.bakeVanilla(context.getBaseModel(), baker, modelState));
    }

    public static final class Baked extends BakedModelWrapper<BakedModel> {

        public Baked(BakedModel originalModel) {
            super(originalModel);
        }

        @Override
        public boolean isCustomRenderer() {
            return true;
        }
    }

    public static final class Loader implements IGeometryLoader<CustomItemRendererWrapperModel> {

        public static final CustomItemRendererWrapperModel.Loader INSTANCE = new CustomItemRendererWrapperModel.Loader();

        private Loader() {}

        @Override
        public CustomItemRendererWrapperModel read(JsonObject json, JsonDeserializationContext context) {
            // the rest of the JSON is a normal vanilla model, which the context hands to bake
            return new CustomItemRendererWrapperModel();
        }
    }

    public static final class Builder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

        public Builder(T parent, ExistingFileHelper existingFileHelper) {
            super(ID, parent, existingFileHelper, true);
        }

        public static <T extends ModelBuilder<T>> Builder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
            return new Builder<>(parent, existingFileHelper);
        }
    }
}
