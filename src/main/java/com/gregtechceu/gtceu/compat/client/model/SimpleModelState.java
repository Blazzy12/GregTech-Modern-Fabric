package com.gregtechceu.gtceu.compat.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * A model state of a rotation and UV lock. Right-angle rotations use vanilla's {@link BlockModelRotation}, which knows
 * how to lock UVs; other rotations transform the geometry only.
 */
public final class SimpleModelState implements ModelState {

    private final Transformation rotation;
    private final @Nullable ModelState vanilla;

    public SimpleModelState(Transformation rotation, boolean uvLock) {
        this.rotation = rotation;
        OctahedralGroup group = findGroup(rotation);
        if (group != null) {
            BlockModelRotation blockRotation = BlockModelRotation.get(group);
            this.vanilla = uvLock ? blockRotation.withUvLock() : blockRotation;
        } else {
            this.vanilla = null;
        }
    }

    public SimpleModelState(Transformation rotation) {
        this(rotation, false);
    }

    /** @return the octahedral group with the rotation's matrix, or {@code null} if it isn't a right-angle rotation */
    public static @Nullable OctahedralGroup findGroup(Transformation rotation) {
        if (Transformation.IDENTITY.equals(rotation)) return OctahedralGroup.IDENTITY;
        Matrix3f matrix = new Matrix3f(rotation.getMatrix());
        for (OctahedralGroup group : OctahedralGroup.values()) {
            if (new Matrix3f(group.transformation()).equals(matrix, 1.0e-4f)) {
                return group;
            }
        }
        return null;
    }

    /** @return the rotation of another model state composed with this one's */
    public static ModelState compose(ModelState first, Transformation second, boolean uvLock) {
        return new SimpleModelState(first.transformation().compose(second), uvLock);
    }

    @Override
    public Transformation transformation() {
        return vanilla != null ? vanilla.transformation() : rotation;
    }

    @Override
    public Matrix4fc faceTransformation(Direction face) {
        return vanilla != null ? vanilla.faceTransformation(face) : NO_TRANSFORM;
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction face) {
        return vanilla != null ? vanilla.inverseFaceTransformation(face) : NO_TRANSFORM;
    }

    /** @return the rotation matrix */
    public Matrix4f getMatrix() {
        return new Matrix4f(transformation().getMatrix());
    }
}
