package com.gregtechceu.gtceu.utils;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;
import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.Map;

public class GTMatrixUtils {

    private static final Map<Direction, Vector3fc> directionAxes = Util.make(new EnumMap<>(Direction.class), map -> {
        for (Direction dir : GTUtil.DIRECTIONS) {
            map.put(dir, dir.step());
        }
    });
    private static final Table<Direction, Direction, Matrix4fc> rotations = Tables
            .synchronizedTable(HashBasedTable.create());

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @return the angle of rotation to make {@code from} point in the direction of {@code to}
     */
    @Contract(pure = true)
    public static float getRotationAngle(final Vector3fc from, final Vector3fc to) {
        return (float) Math.acos(from.dot(to));
    }

    /**
     * This method isn't pure, {@code from} will be modified!
     * 
     * @param from the original vector
     * @param to   the wanted vector
     * @return the axis of rotation to make {@code from} point in the direction of {@code to}
     */
    public static Vector3f getRotationAxis(Vector3f from, final Vector3fc to) {
        return getRotationAxis(from, to, from);
    }

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @param dest the vector to save the result to
     * @return {@code dest}
     */
    public static Vector3f getRotationAxis(final Vector3fc from, final Vector3fc to, Vector3f dest) {
        return from.cross(to, dest).normalize();
    }

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @return the quaternion to make {@code from} point in the direction of {@code to}
     */
    @Contract(pure = true)
    public static Quaternionf getRotation(final Vector3fc from, final Vector3fc to) {
        return from.rotationTo(to, new Quaternionf());
    }

    /**
     * @param from the original direction
     * @param to   the wanted direction
     * @return the quaternion to make a vector based on {@code from} point towards {@code to}
     */
    @Contract(pure = true)
    public static Quaternionf getRotation(final Direction from, final Direction to) {
        return getRotation(getDirectionAxis(from), getDirectionAxis(to));
    }

    /**
     * Transforms the {@code matrix} and all {@code additional} vectors such that the {@code from} vector will be on the
     * {@code to} vector's axis
     * 
     * @param matrix     the matrix to transform
     * @param from       the original vector
     * @param to         the destination vector
     * @param additional additional vectors to transform
     */
    public static void rotateMatrix(Matrix4f matrix, Vector3f from, Vector3fc to, Vector3f... additional) {
        if (from.equals(to)) {
            return;
        }
        if (-from.x() == to.x() && -from.y() == to.y() && -from.z() == to.z()) {
            rotateMatrix(matrix, Mth.PI, getDirectionAxis(Direction.UP), additional);
        } else {
            var angle = getRotationAngle(from, to);
            getRotationAxis(from, to);
            rotateMatrix(matrix, angle, from, additional);
        }
    }

    /**
     * @param matrix     the matrix to transform
     * @param angle      the angle of rotation (radians)
     * @param axis       axis of rotation
     * @param additional additional vectors to transform
     */
    public static void rotateMatrix(Matrix4f matrix, float angle, Vector3fc axis, Vector3f... additional) {
        matrix.rotate(angle, axis);
        for (var vec : additional) {
            vec.rotateAxis(angle, axis.x(), axis.y(), axis.z());
        }
    }

    /**
     * @param upward the {@code upwardFacing} of the machine
     * @return the angle of rotation (in radians) along the front face axis to get the correct orientation
     */
    public static float upwardFacingAngle(Direction upward) {
        return switch (upward) {
            case NORTH -> 0;
            case SOUTH -> 2;
            case WEST -> 3;
            case EAST -> 1;
            default -> throw new InvalidParameterException("Upward facing can't be up/down");
        } * Mth.HALF_PI;
    }

    /**
     * Computes the roll angle around the {@code frontFacing} axis needed to orient a model - whose top points along
     * {@code referenceUp} when no roll is applied - so that its top points toward the (global) {@code upwardsFacing}.
     * <p>
     * Unlike {@link #upwardFacingAngle(Direction)}, this works directly with the global upwards facing and therefore
     * accepts any of the six directions (including {@link Direction#UP UP}/{@link Direction#DOWN DOWN}).
     *
     * @param frontFacing   the front facing of the machine
     * @param upwardsFacing the global upwards facing of the machine
     * @param referenceUp   the direction the model's top points in when no roll is applied
     * @return the roll angle in radians around the front-facing axis
     */
    public static float frontAxisRollAngle(Direction frontFacing, Direction upwardsFacing, Direction referenceUp) {
        Vector3fc frontAxis = getDirectionAxis(frontFacing);
        Vector3f actualUp = upwardsFacing.step();
        Vector3f reference = referenceUp.step();
        float sin = reference.cross(actualUp, new Vector3f()).dot(frontAxis);
        float cos = reference.dot(actualUp);
        return (float) Math.atan2(sin, cos);
    }

    public static Vector3f rotateMatrixToFront(Matrix4f matrix, Direction frontFace) {
        // rotate frontFacing to correct cardinal direction
        Vector3f front = frontFace.step();
        rotateMatrix(matrix, Direction.NORTH.step(), getDirectionAxis(frontFace), front);
        return front;
    }

    public static void rotateMatrixToUp(Matrix4f matrix, Vector3fc front, Direction upwardsFace) {
        // rotate upwards face to the correct orientation
        rotateMatrix(matrix, upwardFacingAngle(upwardsFace), front);
    }

    public static Matrix4fc createRotationState(Direction frontFace, Direction upwardFace) {
        if (rotations.contains(frontFace, upwardFace)) {
            var rotation = rotations.get(frontFace, upwardFace);
            assert rotation != null;
            return rotation;
        }
        var matrix = new Matrix4f();
        var front = rotateMatrixToFront(matrix, frontFace);
        front.absolute();
        rotateMatrixToUp(matrix, front, adjustUpwardsToLocal(frontFace, upwardFace.getOpposite()));
        rotations.put(frontFace, upwardFace, matrix);
        return matrix;
    }

    public static Vector3fc getDirectionAxis(Direction dir) {
        return directionAxes.get(dir);
    }

    /**
     * {@link Matrix4f#lookAt(Vector3fc, Vector3fc, Vector3fc) Matrix4f#lookAt} with an up axis of {@code (0, 1, 0)}
     *
     * @see Matrix4f#lookAt(Vector3fc, Vector3fc, Vector3fc)
     */
    public static Matrix4f lookAt(Vector3fc eyePos, Vector3fc target) {
        return new Matrix4f().lookAt(eyePos, target, GTMath.UNIT_Y);
    }

    /**
     * Make the pose stack's topmost transformation look at a point
     *
     * @param poseStack the pose stack to modify
     * @param eyePos    the position of the camera
     * @param target    the point to look at
     */
    public static void lookAt(PoseStack poseStack, Vector3fc eyePos, Vector3fc target) {
        lookAt(poseStack.last(), eyePos, target);
    }

    /**
     * Make the pose stack's topmost transformation look at a point
     *
     * @param pose   the pose stack layer to modify
     * @param eyePos the position of the camera
     * @param target the point to look at
     */
    public static void lookAt(PoseStack.Pose pose, Vector3fc eyePos, Vector3fc target) {
        pose.pose().lookAt(eyePos, target, GTMath.UNIT_Y);
        pose.normal().lookAlong(target, GTMath.UNIT_Y);
    }

    public static Direction adjustUpwardsToLocal(Direction frontFacing, Direction upwardFacing) {
        if (frontFacing.getAxis() == Direction.Axis.Y) {
            return frontFacing.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                    upwardFacing : upwardFacing.getOpposite();
        } else if (frontFacing.getAxis() == Direction.Axis.Z) {
            if (upwardFacing.getAxis() == Direction.Axis.Y) {
                return Direction.fromAxisAndDirection(Direction.Axis.Z, upwardFacing.getAxisDirection());
            } else if (upwardFacing.getAxis() == Direction.Axis.X) {
                Direction.AxisDirection dir = frontFacing.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                        upwardFacing.getAxisDirection().opposite() : upwardFacing.getAxisDirection();
                return Direction.fromAxisAndDirection(Direction.Axis.X, dir);
            }
        } else if (frontFacing.getAxis() == Direction.Axis.X) {
            if (upwardFacing.getAxis() == Direction.Axis.Y) {
                return Direction.fromAxisAndDirection(Direction.Axis.Z, upwardFacing.getAxisDirection());
            } else if (upwardFacing.getAxis() == Direction.Axis.Z) {
                Direction.AxisDirection dir = frontFacing.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                        upwardFacing.getAxisDirection().opposite() : upwardFacing.getAxisDirection();
                return Direction.fromAxisAndDirection(Direction.Axis.X, dir);
            }
        }
        return Direction.NORTH;
    }
}
