package com.gregtechceu.gtceu.compat.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

/**
 * An unbaked model in the shape of the pre-26.x API GregTech's models are written against. Vanilla models are wrapped
 * by {@link VanillaUnbakedModel}; models with custom loaders are in {@code geometry}.
 */
public interface UnbakedModel {

    /** @return the models this one refers to, which have to be loaded */
    Collection<Identifier> getDependencies();

    /** Looks up the models this one refers to. Called before {@link #bake}. */
    void resolveParents(Function<Identifier, UnbakedModel> resolver);

    @Nullable
    BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state);
}
