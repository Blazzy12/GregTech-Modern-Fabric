package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;







/** Renders a primed GregTech explosive like primed TNT, with its own block. */
public class GTExplosiveRenderer<T extends GTExplosiveEntity> extends EntityRenderer<T, TntRenderState> {

    private final BlockModelResolver blockModelResolver;

    public GTExplosiveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public TntRenderState createRenderState() {
        return new TntRenderState();
    }

    @Override
    public void extractRenderState(T entity, TntRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.fuseRemainingInTicks = entity.getFuse() - partialTicks + 1.0F;
        this.blockModelResolver.update(state.blockState, entity.getExplosiveState(),
                TntRenderer.BLOCK_DISPLAY_CONTEXT);
    }

    @Override
    public void submit(TntRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        float fuse = state.fuseRemainingInTicks;
        if (fuse < 10.0F) {
            float scale = 1.0F + TntRenderer.getSwellAmount(fuse);
            poseStack.scale(scale, scale, scale);
        }

        poseStack.rotateDegrees(Axis.YP, -90.0F);
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        if (!state.blockState.isEmpty()) {
            TntMinecartRenderer.submitWhiteSolidBlock(state.blockState, poseStack, collector, state.lightCoords,
                    TntRenderer.isLit(fuse), state.outlineColor);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
