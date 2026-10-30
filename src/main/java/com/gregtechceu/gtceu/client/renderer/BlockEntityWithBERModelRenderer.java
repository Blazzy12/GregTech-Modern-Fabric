package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;
import com.gregtechceu.gtceu.compat.client.model.BakedModel;
import com.gregtechceu.gtceu.compat.client.model.GTBlockStateModel;
import com.gregtechceu.gtceu.compat.client.render.LegacyBlockEntityRenderer;
import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

/** Renders block entities whose block model is also their renderer ({@link IBlockEntityRendererBakedModel}). */
public class BlockEntityWithBERModelRenderer<T extends BlockEntity> extends LegacyBlockEntityRenderer<T> {

    public BlockEntityWithBERModelRenderer(BlockEntityRendererProvider.Context context) {}

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> @Nullable IBlockEntityRendererBakedModel<T> getModel(T blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        var stateModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        if (stateModel instanceof GTBlockStateModel gtModel) {
            BakedModel model = gtModel.model();
            if (model instanceof IBlockEntityRendererBakedModel<?> berModel &&
                    (berModel.getBlockEntityType() == null || berModel.getBlockEntityType() == blockEntity.getType())) {
                return (IBlockEntityRendererBakedModel<T>) berModel;
            }
        }
        return null;
    }

    @Override
    protected void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                          int packedLight, int packedOverlay) {
        var model = getModel(blockEntity);
        if (model != null) {
            model.render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // the models decide per block entity, which 26.x doesn't ask anymore; large renders (fusion rings, ...)
        // must not be culled with their block
        return true;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        var model = getModel(blockEntity);
        return model != null && model.shouldRender(blockEntity, cameraPos);
    }
}
