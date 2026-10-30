package com.gregtechceu.gtceu.compat.client.render;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

/**
 * A block entity renderer written the pre-26.x way, drawing into a {@link MultiBufferSource}. It draws while the
 * render state is extracted (in block space), and the recorded geometry is submitted with the block's pose.
 */
public abstract class LegacyBlockEntityRenderer<T extends BlockEntity>
                                               implements BlockEntityRenderer<T, LegacyBlockEntityRenderer.State> {

    protected abstract void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                                   int packedLight, int packedOverlay);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T blockEntity, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.buffers.clear();
        render(blockEntity, partialTicks, new PoseStack(), state.buffers, state.lightCoords,
                OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        state.buffers.submit(poseStack, collector);
    }

    public static class State extends BlockEntityRenderState {

        public final RecordingBufferSource buffers = new RecordingBufferSource();
    }
}
