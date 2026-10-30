package com.gregtechceu.gtceu.compat.client.model.geometry;

import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import com.gregtechceu.gtceu.compat.client.model.ItemOverrides;
import com.gregtechceu.gtceu.compat.client.model.UnbakedModel;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/** The custom part of a model read by an {@link IGeometryLoader}. */
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> {

    BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                    Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                    ItemOverrides overrides);

    /** Looks up the models this geometry refers to. Called before {@link #bake}. */
    default void resolveParents(Function<Identifier, UnbakedModel> modelGetter, IGeometryBakingContext context) {}

    /** @return the models this geometry refers to, which have to be loaded */
    default Collection<Identifier> getDependencies() {
        return List.of();
    }
}
