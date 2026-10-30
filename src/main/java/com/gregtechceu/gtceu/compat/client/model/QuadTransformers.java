package com.gregtechceu.gtceu.compat.client.model;

import com.mojang.math.Transformation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Common {@link IQuadTransformer}s, like NeoForge's. */
public final class QuadTransformers {

    private static final IQuadTransformer EMPTY = quad -> {};

    private QuadTransformers() {}

    public static IQuadTransformer empty() {
        return EMPTY;
    }

    /** Applies a transformation to the positions and normals, around the block centre. */
    public static IQuadTransformer applying(Transformation transform) {
        if (Transformation.IDENTITY.equals(transform)) return empty();
        Matrix4f matrix = new Matrix4f(transform.getMatrix());
        Matrix3f normalMatrix = new Matrix3f(matrix).invert().transpose();
        return quad -> {
            int[] vertices = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
                Vector4f pos = new Vector4f(Float.intBitsToFloat(vertices[offset]) - 0.5f,
                        Float.intBitsToFloat(vertices[offset + 1]) - 0.5f,
                        Float.intBitsToFloat(vertices[offset + 2]) - 0.5f, 1f);
                matrix.transform(pos);
                vertices[offset] = Float.floatToRawIntBits(pos.x / pos.w + 0.5f);
                vertices[offset + 1] = Float.floatToRawIntBits(pos.y / pos.w + 0.5f);
                vertices[offset + 2] = Float.floatToRawIntBits(pos.z / pos.w + 0.5f);

                int normalOffset = i * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL;
                int packed = vertices[normalOffset];
                if (packed != 0) {
                    Vector3f normal = new Vector3f(((byte) packed) / 127f, ((byte) (packed >> 8)) / 127f,
                            ((byte) (packed >> 16)) / 127f);
                    normalMatrix.transform(normal).normalize();
                    vertices[normalOffset] = (packed & 0xFF000000) |
                            BakedQuad.packNormal(normal.x, normal.y, normal.z);
                }
            }
        };
    }

    /** Sets the lightmap of every vertex. */
    public static IQuadTransformer applyingLightmap(int packedLight) {
        return quad -> {
            int[] vertices = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.UV2] = packedLight;
            }
        };
    }

    public static IQuadTransformer applyingLightmap(int blockLight, int skyLight) {
        return applyingLightmap(blockLight << 4 | skyLight << 20);
    }

    /** Sets the colour of every vertex, as ARGB. */
    public static IQuadTransformer applyingColor(int argb) {
        int abgr = toABGR(argb);
        return quad -> {
            int[] vertices = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.COLOR] = abgr;
            }
        };
    }

    /** Makes the quad emit light, by raising its lightmap to at least the emissivity. */
    public static IQuadTransformer settingEmissivity(int emissivity) {
        if (emissivity <= 0) return empty();
        return quad -> {
            int[] vertices = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.UV2;
                int light = vertices[offset];
                int block = Math.max(light & 0xFFFF, emissivity << 4);
                int sky = Math.max(light >>> 16, emissivity << 4);
                vertices[offset] = block | sky << 16;
            }
        };
    }

    public static IQuadTransformer settingMaxEmissivity() {
        return settingEmissivity(15);
    }

    /** Swaps the red and blue channel, converting between ARGB and the ABGR the vertex data uses. */
    public static int toABGR(int argb) {
        return (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >> 16) | ((argb & 0x000000FF) << 16);
    }
}
