package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;
import com.gregtechceu.gtceu.compat.client.model.GTBlockStateModel;
import com.gregtechceu.gtceu.compat.client.render.RecordingBufferSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Renders the dynamic part of a machine item (what its block entity renderer draws, like stored items or fluids).
 * Machine item definitions combine it with the machine's static model as {@code "type": "gtceu:machine_dynamic"}.
 */
public class MachineItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {

    public static final MachineItemSpecialRenderer INSTANCE = new MachineItemSpecialRenderer();

    @Override
    public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (stack == null || !(stack.getItem() instanceof BlockItem blockItem)) return;
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                .get(blockItem.getBlock().defaultBlockState());
        if (!(model instanceof GTBlockStateModel gtModel) ||
                !(gtModel.model() instanceof IBlockEntityRendererBakedModel<?> berModel)) {
            return;
        }
        RecordingBufferSource buffers = new RecordingBufferSource();
        berModel.renderByItem(stack, ItemDisplayContext.NONE, new PoseStack(), buffers, lightCoords, overlayCoords);
        buffers.submit(poseStack, collector);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            return INSTANCE;
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
