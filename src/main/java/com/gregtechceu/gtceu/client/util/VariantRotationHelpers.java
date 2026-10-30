package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;

import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;

/**
 * Rotations of block model variants, including a z rotation. 26.x blockstates have {@code x}, {@code y} and
 * {@code z} natively, so this uses the same rotations they do.
 */
public class VariantRotationHelpers {

    private VariantRotationHelpers() {}

    public static Transformation getRotationTransform(int xRot, int yRot, int zRot) {
        return BlockModelRotation.get(Quadrant.fromXYZAngles(Quadrant.parseJson(xRot), Quadrant.parseJson(yRot),
                Quadrant.parseJson(zRot))).transformation();
    }
}
