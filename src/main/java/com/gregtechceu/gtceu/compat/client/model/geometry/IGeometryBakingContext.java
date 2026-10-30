package com.gregtechceu.gtceu.compat.client.model.geometry;

import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.Material;

import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;

/** What a geometry gets from the JSON model around it: textures, transforms and flags. */
public interface IGeometryBakingContext {

    String getModelName();

    boolean hasMaterial(String name);

    @Nullable
    Material getMaterial(String name);

    boolean isGui3d();

    boolean useBlockLight();

    boolean useAmbientOcclusion();

    ItemTransforms getTransforms();

    /** @return the plain JSON part of the model (parent, textures, elements), resolved */
    ResolvedModel getBaseModel();

    default Transformation getRootTransform() {
        return Transformation.IDENTITY;
    }
}
