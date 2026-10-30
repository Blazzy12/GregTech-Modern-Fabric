package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.client.model.FaceLayer;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import com.mojang.blaze3d.platform.Transparency;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Arrays;

/**
 * A quad in the pre-26.x vertex layout (position, colour, uv, lightmap, normal; see {@link IQuadTransformer}), which
 * GregTech's models and quad transformers are written against.
 * <p>
 * 26.x quads are immutable records without per-vertex colour or light. These quads keep both, and are handed to the
 * Fabric renderer by {@link #emit}; {@link #of} and {@link #toVanilla} convert from and to vanilla quads where vanilla
 * geometry is needed (model parts baked from JSON, item models).
 */
public class BakedQuad {

    protected final int[] vertices;
    protected final int tintIndex;
    protected final Direction direction;
    protected final TextureAtlasSprite sprite;
    protected final boolean shade;
    protected final int lightEmission;
    protected final boolean hasAmbientOcclusion;

    private @Nullable String textureKey;
    private FaceLayer faceLayer = FaceLayer.UNCLASSIFIED;
    /** the render layer of a quad converted from a vanilla quad */
    private @Nullable ChunkSectionLayer layer;

    public BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade) {
        this(vertices, tintIndex, direction, sprite, shade, 0, true);
    }

    public BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade,
                     boolean hasAmbientOcclusion) {
        this(vertices, tintIndex, direction, sprite, shade, 0, hasAmbientOcclusion);
    }

    public BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade,
                     int lightEmission, boolean hasAmbientOcclusion) {
        this.vertices = vertices;
        this.tintIndex = tintIndex;
        this.direction = direction;
        this.sprite = sprite;
        this.shade = shade;
        this.lightEmission = lightEmission;
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    /** Converts a vanilla quad: white, unlit vertices with normals from the face direction. */
    public static BakedQuad of(net.minecraft.client.resources.model.geometry.BakedQuad quad) {
        int[] vertices = new int[IQuadTransformer.STRIDE * 4];
        Direction direction = quad.direction();
        int normal = packNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE;
            Vector3fc pos = quad.position(i);
            vertices[offset + IQuadTransformer.POSITION] = Float.floatToRawIntBits(pos.x());
            vertices[offset + IQuadTransformer.POSITION + 1] = Float.floatToRawIntBits(pos.y());
            vertices[offset + IQuadTransformer.POSITION + 2] = Float.floatToRawIntBits(pos.z());
            vertices[offset + IQuadTransformer.COLOR] = -1;
            long uv = quad.packedUV(i);
            vertices[offset + IQuadTransformer.UV0] = Float.floatToRawIntBits(UVPair.unpackU(uv));
            vertices[offset + IQuadTransformer.UV0 + 1] = Float.floatToRawIntBits(UVPair.unpackV(uv));
            vertices[offset + IQuadTransformer.UV2] = 0;
            vertices[offset + IQuadTransformer.NORMAL] = normal;
        }
        var info = quad.materialInfo();
        BakedQuad converted = new BakedQuad(vertices, info.tintIndex(), direction, info.sprite(),
                info.shadeDirectionOverride() == null, info.lightEmission(), true);
        converted.layer = info.layer();
        return converted;
    }

    public int[] getVertices() {
        return vertices;
    }

    public boolean isTinted() {
        return tintIndex != -1;
    }

    /**
     * GT models mark the emissive variant of tint layer {@code n} with {@code -101 - 10n} (for Shimmer). 26.x tints
     * are an indexed list, so those map back to {@code n} and any other negative index to none.
     */
    public static int vanillaTintIndex(int tintIndex) {
        if (tintIndex <= -101) return (-tintIndex - 101) / 10;
        return Math.max(tintIndex, -1);
    }

    public int getTintIndex() {
        return tintIndex;
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isShade() {
        return shade;
    }

    public int getLightEmission() {
        return lightEmission;
    }

    public boolean hasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    /**
     * @return the render layer this quad came with from vanilla, or else the one its sprite needs
     */
    public ChunkSectionLayer getLayer() {
        if (layer == null) {
            layer = ChunkSectionLayer.byTransparency(sprite.contents().computeTransparency(0, 0, 1, 1));
        }
        return layer;
    }

    public BakedQuad gtceu$setTextureKey(@Nullable String key) {
        this.textureKey = key;
        return this;
    }

    public @Nullable String gtceu$getTextureKey() {
        return textureKey;
    }

    public BakedQuad gtceu$setFaceLayer(FaceLayer layer) {
        this.faceLayer = layer;
        return this;
    }

    public FaceLayer gtceu$getFaceLayer() {
        return faceLayer;
    }

    /** Copies the GregTech metadata (texture key, face layer and source layer) of another quad onto this one. */
    public BakedQuad copyMetadataFrom(BakedQuad other) {
        this.textureKey = other.textureKey;
        this.faceLayer = other.faceLayer;
        this.layer = other.layer;
        return this;
    }

    public float getX(int vertex) {
        return Float.intBitsToFloat(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION]);
    }

    public float getY(int vertex) {
        return Float.intBitsToFloat(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 1]);
    }

    public float getZ(int vertex) {
        return Float.intBitsToFloat(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 2]);
    }

    public float getU(int vertex) {
        return Float.intBitsToFloat(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0]);
    }

    public float getV(int vertex) {
        return Float.intBitsToFloat(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0 + 1]);
    }

    /** @return the colour of a vertex as ARGB */
    public int getColor(int vertex) {
        return QuadTransformers.toABGR(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR]);
    }

    public int getLight(int vertex) {
        return vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV2];
    }

    /**
     * Emits this quad to the Fabric renderer.
     *
     * @param layer    the chunk layer to render in, or {@code null} for {@link #getLayer()}
     * @param cullFace the face to cull this quad by, or {@code null}
     */
    public void emit(QuadEmitter emitter, @Nullable ChunkSectionLayer layer, @Nullable Direction cullFace) {
        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE;
            emitter.pos(i, getX(i), getY(i), getZ(i));
            emitter.uv(i, getU(i), getV(i));
            emitter.color(i, getColor(i));
            emitter.lightmap(i, vertices[offset + IQuadTransformer.UV2]);
            int normal = vertices[offset + IQuadTransformer.NORMAL];
            if (normal != 0) {
                emitter.normal(i, unpackNormal(normal, 0), unpackNormal(normal, 8), unpackNormal(normal, 16));
            }
        }
        emitter.nominalFace(direction);
        emitter.cullFace(cullFace);
        emitter.chunkLayer(layer != null ? layer : getLayer());
        emitter.tintIndex(vanillaTintIndex(tintIndex));
        if (!shade) emitter.shadeDirectionOverride(Direction.UP);
        if (lightEmission >= 15) emitter.emissive(true);
        else if (lightEmission > 0) emitter.minLightmap(lightEmission << 4 | lightEmission << 20);
        emitter.ambientOcclusion(hasAmbientOcclusion ? net.fabricmc.fabric.api.util.TriState.DEFAULT :
                net.fabricmc.fabric.api.util.TriState.FALSE);
        emitter.emit();
    }

    /**
     * Writes this quad to a vertex buffer, like the removed {@code VertexConsumer#putBulkData}.
     *
     * @param tint the tint colour (ARGB) to multiply the vertex colours with, -1 for none
     */
    public void putInto(com.mojang.blaze3d.vertex.VertexConsumer buffer,
                        com.mojang.blaze3d.vertex.PoseStack.Pose pose, int tint, int light, int overlay) {
        Vector3f pos = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int i = 0; i < 4; i++) {
            pose.pose().transformPosition(getX(i), getY(i), getZ(i), pos);
            int packed = vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL];
            if (packed != 0) {
                pose.transformNormal(unpackNormal(packed, 0), unpackNormal(packed, 8), unpackNormal(packed, 16),
                        normal);
            } else {
                pose.transformNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ(), normal);
            }
            int color = getColor(i);
            if (tint != -1) color = net.minecraft.util.ARGB.multiply(color, tint);
            int vertexLight = Math.max(light & 0xFFFF, getLight(i) & 0xFFFF) |
                    Math.max(light >>> 16, getLight(i) >>> 16) << 16;
            if (lightEmission > 0) vertexLight = net.minecraft.util.LightCoordsUtil.lightCoordsWithEmission(
                    vertexLight, lightEmission);
            buffer.addVertex(pos.x, pos.y, pos.z, color, getU(i), getV(i), overlay, vertexLight, normal.x, normal.y,
                    normal.z);
        }
    }

    /**
     * Converts to a vanilla quad, dropping per-vertex colour and light.
     *
     * @param layer the render layer, or {@code null} for {@link #getLayer()}
     */
    public net.minecraft.client.resources.model.geometry.BakedQuad toVanilla(@Nullable ChunkSectionLayer layer) {
        ChunkSectionLayer renderLayer = layer != null ? layer : getLayer();
        Transparency transparency = switch (renderLayer) {
            case SOLID -> Transparency.NONE;
            case CUTOUT -> Transparency.TRANSPARENT;
            case TRANSLUCENT -> Transparency.TRANSLUCENT;
        };
        var info = net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo.of(
                new Material.Baked(sprite, renderLayer == ChunkSectionLayer.TRANSLUCENT), transparency,
                vanillaTintIndex(tintIndex),
                shade ? null : Direction.UP, lightEmission);
        Vector3fc[] pos = new Vector3fc[4];
        long[] uv = new long[4];
        for (int i = 0; i < 4; i++) {
            pos[i] = new Vector3f(getX(i), getY(i), getZ(i));
            uv[i] = UVPair.pack(getU(i), getV(i));
        }
        return new net.minecraft.client.resources.model.geometry.BakedQuad(pos[0], pos[1], pos[2], pos[3],
                uv[0], uv[1], uv[2], uv[3], direction, info);
    }

    public static int packNormal(float x, float y, float z) {
        return ((int) (x * 127.0f) & 0xFF) | (((int) (y * 127.0f) & 0xFF) << 8) |
                (((int) (z * 127.0f) & 0xFF) << 16);
    }

    private static float unpackNormal(int packed, int shift) {
        return ((byte) (packed >> shift)) / 127.0f;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BakedQuad that)) return false;
        return shade == that.shade && hasAmbientOcclusion == that.hasAmbientOcclusion &&
                tintIndex == that.tintIndex && lightEmission == that.lightEmission &&
                direction == that.direction && sprite == that.sprite && Arrays.equals(vertices, that.vertices);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(shade);
        result = 31 * result + Boolean.hashCode(hasAmbientOcclusion);
        result = 31 * result + tintIndex;
        result = 31 * result + direction.hashCode();
        result = 31 * result + sprite.hashCode();
        result = 31 * result + Arrays.hashCode(vertices);
        return result;
    }
}
