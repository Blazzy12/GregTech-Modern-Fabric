package brachy.modularui.drawable.schema;


import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Accessors(fluent = true, chain = true)
public class BlockHighlight {

    // rendnering magic

    protected static final float[][] vertices = new float[6][12];

    static {
        int[][] intVertices = {
                {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0},
                {1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0}
        };
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                int v = intVertices[i][j];
                vertices[i][j] = v == 1 ? 1.005f : -0.005f;
            }
        }
    }

    @Getter
    @Setter
    private int color;

    @Getter
    @Setter
    private boolean allSides;

    @Getter
    @Setter
    private float thickness;

    public BlockHighlight(int color) {
        this(color, true);
    }

    public BlockHighlight(int color, float frameThickness) {
        this(color, true, frameThickness);
    }

    public BlockHighlight(int color, boolean allSides) {
        this(color, allSides, 0.0f);
    }

    public BlockHighlight(int color, boolean allSides, float frameThickness) {
        this.color = color;
        this.allSides = allSides;
        this.thickness = frameThickness;
    }

    public final void renderHighlight(PoseStack pose, SubmitNodeCollector collector, @Nullable BlockHitResult result,
                                      Vector3f camera) {
        if (result != null && result.getType() == HitResult.Type.BLOCK) {
            renderHighlight(pose, collector, result.getBlockPos(), result.getDirection(), camera);
        }
    }

    public void renderHighlight(PoseStack poseStack, SubmitNodeCollector collector, BlockPos pos,
                                Direction direction, Vector3f camera) {
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        float distance = camera.distance(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        Direction side = this.allSides ? null : direction;
        int color = this.color;
        float thickness = this.thickness;
        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            if (thickness >= 0) {
                // scale frame thickness with distance to camera
                float offset = (float) (thickness * (1 + Math.max(0, Math.sqrt(distance) - 3) / 5.0f));
                renderFrame(pose.pose(), buffer, side, offset, color);
            } else {
                renderSolid(pose.pose(), buffer, side, color);
            }
        });
        poseStack.popPose();
    }

    public static void renderSolid(Matrix4f pose, VertexConsumer buffer, @Nullable Direction direction, int color) {
        if (direction == null) {
            for (Direction dir : Direction.values()) {
                buildFace(pose, buffer, dir, color);
            }
        } else {
            buildFace(pose, buffer, direction, color);
        }
    }

    protected static void renderFrame(Matrix4f pose, VertexConsumer buffer, @Nullable Direction side, float offset,
                                      int color) {
        if (side == null) {
            for (Direction dir : Direction.values()) {
                buildFrameFace(pose, buffer, dir, offset, color);
            }
        } else {
            buildFrameFace(pose, buffer, side, offset, color);
        }
    }

    /** One quad per frame edge, between the outer corners and the inset inner corners. */
    protected static void buildFrameFace(Matrix4f pose, VertexConsumer buffer, @NotNull Direction side, float offset,
                                         int color) {
        float[] vert = vertices[side.get3DDataValue()];
        int[] corners = { 9, 6, 3, 0, 9 };
        for (int i = 0; i < 4; i++) {
            int a = corners[i], b = corners[i + 1];
            buildVertex(pose, buffer, vert, a, color);
            buildInnerVertex(pose, buffer, vert, a, side, offset, color);
            buildInnerVertex(pose, buffer, vert, b, side, offset, color);
            buildVertex(pose, buffer, vert, b, color);
        }
    }

    protected static void buildVertex(Matrix4f pose, VertexConsumer buffer, float[] vertices, int vertexIndex,
                                      int color) {
        float x = vertices[vertexIndex];
        float y = vertices[vertexIndex + 1];
        float z = vertices[vertexIndex + 2];
        buffer.addVertex(pose, x, y, z).setColor(color);
    }

    private static void buildInnerVertex(Matrix4f pose, VertexConsumer buffer, float[] vertices, int vertexIndex,
                                         @NotNull Direction side, float offset, int color) {
        float x = vertices[vertexIndex];
        float y = vertices[vertexIndex + 1];
        float z = vertices[vertexIndex + 2];
        if (side.getAxis() != Direction.Axis.X) {
            if (x >= 1) x -= offset;
            else x += offset;
        }
        if (side.getAxis() != Direction.Axis.Y) {
            if (y >= 1) y -= offset;
            else y += offset;
        }
        if (side.getAxis() != Direction.Axis.Z) {
            if (z >= 1) z -= offset;
            else z += offset;
        }
        buffer.addVertex(pose, x, y, z).setColor(color);
    }

    protected static void buildFace(Matrix4f pose, VertexConsumer buffer, @NotNull Direction side, int color) {
        float[] vert = vertices[side.ordinal()];
        buildVertex(pose, buffer, vert, 0, color);
        buildVertex(pose, buffer, vert, 3, color);
        buildVertex(pose, buffer, vert, 6, color);
        buildVertex(pose, buffer, vert, 9, color);
    }
}
