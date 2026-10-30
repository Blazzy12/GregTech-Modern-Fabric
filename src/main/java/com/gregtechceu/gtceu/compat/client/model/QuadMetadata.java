package com.gregtechceu.gtceu.compat.client.model;

import com.gregtechceu.gtceu.client.model.FaceLayer;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.QuadCollection;

import com.google.common.collect.MapMaker;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * GregTech metadata of quads baked from JSON: the texture variable a face used (for retexturing) and its
 * {@code gtceu:face_layer}. Vanilla quads and faces are records, so this is kept beside them: face layers by face
 * identity, filled when faces are deserialized, and texture keys only while {@link #capture} bakes a model.
 */
public final class QuadMetadata {

    private static final Map<CuboidFace, FaceLayer> FACE_LAYERS = new MapMaker().weakKeys().concurrencyLevel(4)
            .makeMap();
    private static final ThreadLocal<@Nullable Map<net.minecraft.client.resources.model.geometry.BakedQuad, CuboidFace>> CAPTURED = new ThreadLocal<>();

    private QuadMetadata() {}

    public static void setFaceLayer(CuboidFace face, FaceLayer layer) {
        FACE_LAYERS.put(face, layer);
    }

    public static FaceLayer getFaceLayer(CuboidFace face) {
        return FACE_LAYERS.getOrDefault(face, FaceLayer.UNCLASSIFIED);
    }

    /** Called when a face is baked; remembers its face while {@link #capture} runs. */
    public static void onFaceBaked(net.minecraft.client.resources.model.geometry.BakedQuad quad, CuboidFace face) {
        var captured = CAPTURED.get();
        if (captured != null) captured.put(quad, face);
    }

    /** Bakes vanilla geometry and converts it to a {@link SimpleBakedModel} with the quads' metadata. */
    public static QuadCollection capture(Supplier<QuadCollection> bake,
                                         Map<net.minecraft.client.resources.model.geometry.BakedQuad, CuboidFace> faces) {
        var previous = CAPTURED.get();
        CAPTURED.set(faces);
        try {
            return bake.get();
        } finally {
            CAPTURED.set(previous);
        }
    }

    public static BakedQuad convert(net.minecraft.client.resources.model.geometry.BakedQuad quad) {
        return convert(quad, Map.of());
    }

    public static BakedQuad convert(net.minecraft.client.resources.model.geometry.BakedQuad quad,
                                    Map<net.minecraft.client.resources.model.geometry.BakedQuad, CuboidFace> faces) {
        BakedQuad converted = BakedQuad.of(quad);
        CuboidFace face = faces.get(quad);
        if (face != null) {
            converted.gtceu$setTextureKey(face.texture());
            converted.gtceu$setFaceLayer(getFaceLayer(face));
        }
        return converted;
    }

    public static Map<net.minecraft.client.resources.model.geometry.BakedQuad, CuboidFace> newCapture() {
        return new IdentityHashMap<>();
    }
}
