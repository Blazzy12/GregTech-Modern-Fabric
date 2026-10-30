package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.LampBlockItem;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Renders a lamp item as its block in the state its item data says (inverted, lit, bloom). Used by lamp item
 * definitions as {@code "type": "gtceu:lamp"}.
 */
public class LampSpecialRenderer implements SpecialModelRenderer<BlockState> {

    public static final LampSpecialRenderer INSTANCE = new LampSpecialRenderer();

    @Override
    public void submit(@Nullable BlockState state, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) return;
        MovingBlockRenderState renderState = new MovingBlockRenderState();
        renderState.blockState = state;
        collector.submitMovingBlock(poseStack, renderState, outlineColor);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public @Nullable BlockState extractArgument(ItemStack stack) {
        if (!(stack.getItem() instanceof LampBlockItem item)) return null;
        LampBlockItem.LampData data = stack.getOrDefault(GTDataComponents.LAMP_DATA, LampBlockItem.LampData.EMPTY);
        return item.getBlock().defaultBlockState()
                .setValue(LampBlock.INVERTED, data.inverted())
                .setValue(LampBlock.BLOOM, data.bloom())
                .setValue(LampBlock.LIGHT, data.lit());
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
