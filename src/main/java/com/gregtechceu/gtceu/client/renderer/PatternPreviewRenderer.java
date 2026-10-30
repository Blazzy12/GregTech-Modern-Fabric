package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;

import brachy.modularui.drawable.schema.RenderFilter;
import brachy.modularui.drawable.schema.RenderLevel;
import brachy.modularui.drawable.schema.SchemaMesh;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shows a multiblock's pattern in the world, at its controller, for a while.
 */
public class PatternPreviewRenderer {

    public static final PatternPreviewRenderer INSTANCE = new PatternPreviewRenderer();

    private @Nullable MutableSchema schema;
    private @Nullable SchemaMesh mesh;
    private @Nullable BlockPos controllerPos;
    private final AtomicInteger timeout = new AtomicInteger(-1);
    @Getter
    private final brachy.modularui.drawable.schema.Camera camera = new brachy.modularui.drawable.schema.Camera();

    // Layer to show in the render preview, MAX_INT is show all
    private int layers = Integer.MAX_VALUE;

    /** Registers the tick and render hooks. */
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> INSTANCE.clientTick());
        LevelRenderEvents.COLLECT_SUBMITS.register(INSTANCE::draw);
    }

    public void showPreview(BlockPos controllerPos, MutableSchema schema, RenderFilter renderFilter, int duration) {
        if (this.mesh != null) this.mesh.dispose();
        this.controllerPos = controllerPos;
        this.schema = schema;
        this.mesh = new SchemaMesh(schema, new RenderLevel(schema, renderFilter), () -> renderFilter, () -> {});
        timeout.set(duration);
    }

    public void showPreviewCycleLevel(BlockPos controllerPos, MutableSchema schema, int duration) {
        if (controllerPos.equals(this.controllerPos)) {
            var bounds = schema.getBounds();
            int min = bounds.getFirst().getY();
            int max = bounds.getSecond().getY();
            if (layers == Integer.MAX_VALUE) {
                layers = min;
            } else {
                layers += 1;
                if (layers > max) {
                    layers = Integer.MAX_VALUE;
                }
            }
        } else {
            layers = Integer.MAX_VALUE;
        }
        RenderFilter renderFilter;
        if (layers == Integer.MAX_VALUE) {
            renderFilter = RenderFilter.ALL;
        } else {
            int layer = layers;
            renderFilter = (pos, state) -> pos.getY() == layer;
        }
        showPreview(controllerPos, schema, renderFilter, duration);
    }

    public void clientTick() {
        if (timeout.get() > 0 && timeout.decrementAndGet() <= 0) {
            dispose();
        }
    }

    public void notifyRecompile() {
        if (this.mesh != null) this.mesh.markDirty();
    }

    private void draw(LevelRenderContext context) {
        if (timeout.get() <= 0) return;
        if (this.schema == null || this.controllerPos == null || this.mesh == null) return;

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        Vec3 cameraPos = camera.position();
        this.mesh.update();

        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(controllerPos.getX() - cameraPos.x, controllerPos.getY() - cameraPos.y,
                controllerPos.getZ() - cameraPos.z);
        Vector3f eye = new Vector3f((float) (cameraPos.x - controllerPos.getX()),
                (float) (cameraPos.y - controllerPos.getY()), (float) (cameraPos.z - controllerPos.getZ()));
        this.mesh.submit(poseStack, context.submitNodeCollector(),
                mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), eye, true);
        poseStack.popPose();
    }

    public void dispose() {
        if (this.mesh != null) this.mesh.dispose();
        this.mesh = null;
        this.schema = null;
        this.controllerPos = null;
        this.timeout.set(-1);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PatternPreviewRenderer that)) return false;
        return Objects.equals(this.schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.schema);
    }
}
