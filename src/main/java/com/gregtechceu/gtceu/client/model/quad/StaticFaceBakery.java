package com.gregtechceu.gtceu.client.model.quad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import com.gregtechceu.gtceu.compat.client.model.QuadTransformers;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import com.mojang.math.Quadrant;
import org.joml.Vector3fc;
import com.gregtechceu.gtceu.compat.client.model.BakedQuad;

public class StaticFaceBakery {

    public static final AABB BLOCK = new AABB(0, 0, 0, 1, 1, 1);

    public static final AABB COVER_OVERLAY = BLOCK.inflate(0.014);
    public static final AABB OUTPUT_OVERLAY = BLOCK.inflate(0.018);
    public static final AABB AUTO_OUTPUT_OVERLAY = BLOCK.inflate(0.022);

    public static AABB createFaceCube(Direction face, double thickness) {
        return switch (face) {
            case DOWN -> new AABB(0, 0, 0, 1, thickness, 1);
            case UP -> new AABB(0, 1.0 - thickness, 0, 1, 1, 1);
            case NORTH -> new AABB(0, 0, 0, 1, 1, thickness);
            case SOUTH -> new AABB(0, 0, 1.0 - thickness, 1, 1, 1);
            case WEST -> new AABB(0, 0, 0, thickness, 1, 1);
            case EAST -> new AABB(1.0 - thickness, 0, 0, 1, 1, 1);
        };
    }

    /**
     * bake a quad of specific face.
     * 
     * @param cube       cube model
     * @param face       face of the quad
     * @param sprite     texture
     * @param rotation   additional rotation
     * @param tintIndex  tint color index
     * @param emissivity emissivity
     * @param cull       whether cull the face
     * @param shade      whether shade the face
     */
    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite, boolean cubeUV,
                                     ModelState rotation, int tintIndex, int emissivity, boolean cull, boolean shade) {
        Vector3f posFrom = new Vector3f((float) cube.minX * 16f, (float) cube.minY * 16f, (float) cube.minZ * 16f);
        Vector3f posTo = new Vector3f((float) cube.maxX * 16f, (float) cube.maxY * 16f, (float) cube.maxZ * 16f);
        float[] uv;
        if (cubeUV) {
            uv = switch (face) {
                case UP -> new float[] { posFrom.x(), posFrom.z(), posTo.x(), posTo.z() };
                case DOWN -> new float[] { posFrom.x(), posTo.z(), posTo.x(), posFrom.z() };
                case NORTH -> new float[] { posTo.x(), posTo.y(), posFrom.x(), posFrom.y() };
                case SOUTH -> new float[] { posFrom.x(), posTo.y(), posTo.x(), posFrom.y() };
                case WEST -> new float[] { posFrom.z(), posTo.y(), posTo.z(), posFrom.y() };
                case EAST -> new float[] { posTo.z(), posTo.y(), posFrom.z(), posFrom.y() };
            };
        } else {
            uv = new float[] { 0.0F, 0.0F, 16.0F, 16.0F };
        }

        return bakeQuad(posFrom, posTo, new CuboidFace.UVs(uv[0], uv[1], uv[2], uv[3]), tintIndex,
                sprite.contents().name().toString(), sprite, face, rotation, shade, emissivity);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite, ModelState rotation,
                                     int tintIndex, int emissivity, boolean cull, boolean shade) {
        return bakeFace(cube, face, sprite, false, rotation, tintIndex, emissivity, cull, shade);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation, int tintIndex,
                                     int emissivity, boolean cull, boolean shade) {
        return bakeFace(BLOCK, face, sprite, rotation, tintIndex, emissivity, cull, shade);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation,
                                     int tintIndex, int emissivity) {
        return bakeFace(face, sprite, rotation, tintIndex, emissivity, true, true);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation, int tintIndex) {
        return bakeFace(face, sprite, rotation, tintIndex, 0);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation) {
        return bakeFace(face, sprite, rotation, -1);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite) {
        return bakeFace(face, sprite, BlockModelRotation.IDENTITY);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite, boolean cubeUV) {
        return bakeFace(cube, face, sprite, cubeUV, BlockModelRotation.IDENTITY, -1, 0, true, true);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite) {
        return bakeFace(cube, face, sprite, false);
    }

    /** Interns nothing: faces baked at runtime aren't shared. */
    private static final ModelBaker.Interner NO_INTERNING = new ModelBaker.Interner() {

        @Override
        public Vector3fc vector(Vector3fc vector) {
            return vector;
        }

        @Override
        public net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo materialInfo(
                net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo material) {
            return material;
        }
    };

    /**
     * Bakes one face of a cuboid with vanilla's face bakery (which handles UV lock and winding).
     *
     * @param posFrom    the cuboid's minimum corner, in pixels
     * @param posTo      the cuboid's maximum corner, in pixels
     * @param uvs        the face's UVs, in pixels
     * @param textureKey the texture variable of the face, for retexturing
     */
    public static BakedQuad bakeQuad(Vector3f posFrom, Vector3f posTo, CuboidFace.UVs uvs, int tintIndex,
                                     String textureKey, TextureAtlasSprite sprite, Direction facing,
                                     ModelState transform, boolean shade, int emissivity) {
        var info = net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo.of(
                new Material.Baked(sprite, false), sprite.contents().computeTransparency(0, 0, 1, 1), tintIndex,
                shade ? null : Direction.UP, 0);
        var vanilla = FaceBakery.bakeQuad(NO_INTERNING, posFrom, posTo, uvs, Quadrant.R0, info, facing, transform,
                null);
        BakedQuad quad = BakedQuad.of(vanilla);
        if (emissivity > 0) {
            QuadTransformers.settingEmissivity(emissivity).processInPlace(quad);
        }
        return quad.gtceu$setTextureKey(textureKey);
    }
}
