package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.gregtechceu.gtceu.compat.client.model.BakedQuad;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Consumer;

/**
 * Draws the block on a facade item. The facade item definition combines its plate model with this, as
 * {@code "type": "gtceu:facade"}.
 */
public class FacadeSpecialRenderer implements SpecialModelRenderer<BlockState> {

    public static final FacadeSpecialRenderer INSTANCE = new FacadeSpecialRenderer();

    @Override
    public void submit(@Nullable BlockState state, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) return;
        List<BakedQuad> quads = FacadeCoverRenderer.getItemQuads(state);
        if (quads.isEmpty()) return;
        collector.submitCustomGeometry(poseStack, Sheets.cutoutBlockItemSheet(), (pose, buffer) -> {
            for (BakedQuad quad : quads) {
                quad.putInto(buffer, pose, -1, lightCoords, overlayCoords);
            }
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1 / 16f));
    }

    @Override
    public @Nullable BlockState extractArgument(ItemStack stack) {
        return FacadeItemBehaviour.getFacadeStateNullable(stack);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<BlockState> {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<BlockState> bake(SpecialModelRenderer.BakingContext context) {
            return INSTANCE;
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
