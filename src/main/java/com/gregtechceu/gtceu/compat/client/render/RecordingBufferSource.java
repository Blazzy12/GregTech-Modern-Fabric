package com.gregtechceu.gtceu.compat.client.render;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.Map;

/**
 * Records what is drawn into it, per render type, to submit it later with {@link #submit}. Positions and normals are
 * recorded as given (usually in block space) and transformed by the pose they are submitted with.
 */
public class RecordingBufferSource implements MultiBufferSource {

    private final Map<RenderType, Recording> recordings = new LinkedHashMap<>();
    private final List<Deferred> deferred = new ArrayList<>();

    /** Something submitted directly to the collector (items, text), at the pose it was recorded with. */
    private record Deferred(PoseStack.Pose pose, BiConsumer<PoseStack, SubmitNodeCollector> submit) {}

    /**
     * Records a direct submission, made at submit time with the pose stack's current pose (relative to the pose
     * {@link #submit} is called with).
     */
    public void defer(PoseStack poseStack, BiConsumer<PoseStack, SubmitNodeCollector> submit) {
        deferred.add(new Deferred(poseStack.last().copy(), submit));
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return recordings.computeIfAbsent(renderType, type -> new Recording());
    }

    public boolean isEmpty() {
        if (!deferred.isEmpty()) return false;
        for (Recording recording : recordings.values()) {
            if (!recording.isEmpty()) return false;
        }
        return true;
    }

    public void clear() {
        recordings.clear();
        deferred.clear();
    }

    /** Submits everything recorded, transformed by the pose stack's current pose. */
    public void submit(PoseStack poseStack, SubmitNodeCollector collector) {
        for (var entry : recordings.entrySet()) {
            Recording recording = entry.getValue();
            if (recording.isEmpty()) continue;
            collector.submitCustomGeometry(poseStack, entry.getKey(), recording::replay);
        }
        for (Deferred submission : deferred) {
            poseStack.pushPose();
            poseStack.last().mulPose(submission.pose().pose());
            submission.submit().accept(poseStack, collector);
            poseStack.popPose();
        }
    }

    /** The vertices of one render type. */
    private static final class Recording implements VertexConsumer {

        private static final int FLOATS = 8;
        private static final int INTS = 4;

        // x, y, z, u, v, nx, ny, nz
        private final FloatArrayList floats = new FloatArrayList();
        // color, overlay, light, line width bits
        private final IntArrayList ints = new IntArrayList();
        private int vertices;

        boolean isEmpty() {
            return vertices == 0;
        }

        private int f() {
            return (vertices - 1) * FLOATS;
        }

        private int i() {
            return (vertices - 1) * INTS;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            vertices++;
            floats.add(x);
            floats.add(y);
            floats.add(z);
            floats.add(0);
            floats.add(0);
            floats.add(0);
            floats.add(1);
            floats.add(0);
            ints.add(-1);
            ints.add(0);
            ints.add(0);
            ints.add(Float.floatToRawIntBits(1f));
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return setColor((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
        }

        @Override
        public VertexConsumer setColor(int color) {
            if (vertices > 0) ints.set(i(), color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (vertices > 0) {
                floats.set(f() + 3, u);
                floats.set(f() + 4, v);
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            if (vertices > 0) ints.set(i() + 1, (u & 0xFFFF) | (v & 0xFFFF) << 16);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            if (vertices > 0) ints.set(i() + 2, (u & 0xFFFF) | (v & 0xFFFF) << 16);
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            if (vertices > 0) {
                floats.set(f() + 5, x);
                floats.set(f() + 6, y);
                floats.set(f() + 7, z);
            }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            if (vertices > 0) ints.set(i() + 3, Float.floatToRawIntBits(width));
            return this;
        }

        void replay(PoseStack.Pose pose, VertexConsumer buffer) {
            Vector3f pos = new Vector3f();
            Vector3f normal = new Vector3f();
            for (int v = 0; v < vertices; v++) {
                int fo = v * FLOATS, io = v * INTS;
                pose.pose().transformPosition(floats.getFloat(fo), floats.getFloat(fo + 1), floats.getFloat(fo + 2),
                        pos);
                pose.transformNormal(floats.getFloat(fo + 5), floats.getFloat(fo + 6), floats.getFloat(fo + 7),
                        normal);
                int overlay = ints.getInt(io + 1), light = ints.getInt(io + 2);
                buffer.addVertex(pos.x, pos.y, pos.z)
                        .setColor(ints.getInt(io))
                        .setUv(floats.getFloat(fo + 3), floats.getFloat(fo + 4))
                        .setUv1(overlay & 0xFFFF, overlay >>> 16)
                        .setUv2(light & 0xFFFF, light >>> 16)
                        .setNormal(normal.x, normal.y, normal.z)
                        .setLineWidth(Float.intBitsToFloat(ints.getInt(io + 3)));
            }
        }
    }
}
