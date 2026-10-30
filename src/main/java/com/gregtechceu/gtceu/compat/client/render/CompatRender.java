package com.gregtechceu.gtceu.compat.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item and text drawing for renderers that draw into a {@link MultiBufferSource}, replacing
 * {@code ItemRenderer#renderStatic} and {@code Font#drawInBatch}. They only work with a {@link RecordingBufferSource},
 * where they are submitted later at the current pose.
 */
public final class CompatRender {

    private CompatRender() {}

    public static void renderItem(ItemStack stack, ItemDisplayContext displayContext, int light, int overlay,
                                  PoseStack poseStack, MultiBufferSource buffer, @Nullable Level level, int seed) {
        if (stack.isEmpty() || !(buffer instanceof RecordingBufferSource recording)) return;
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, displayContext, level, null,
                seed);
        recording.defer(poseStack, (pose, collector) -> state.submit(pose, collector, light, overlay, 0));
    }

    public static void drawText(Font font, String text, float x, float y, int color, boolean shadow,
                                PoseStack poseStack, MultiBufferSource buffer, Font.DisplayMode mode,
                                int backgroundColor, int light) {
        drawText(font, FormattedCharSequence.forward(text, Style.EMPTY), x, y, color, shadow, poseStack, buffer,
                mode, backgroundColor, light);
    }

    public static void drawText(Font font, Component text, float x, float y, int color, boolean shadow,
                                PoseStack poseStack, MultiBufferSource buffer, Font.DisplayMode mode,
                                int backgroundColor, int light) {
        drawText(font, text.getVisualOrderText(), x, y, color, shadow, poseStack, buffer, mode, backgroundColor,
                light);
    }

    public static void drawText(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow,
                                PoseStack poseStack, MultiBufferSource buffer, Font.DisplayMode mode,
                                int backgroundColor, int light) {
        if (!(buffer instanceof RecordingBufferSource recording)) return;
        // pre-26 fonts treated a zero alpha as opaque
        int argb = com.gregtechceu.gtceu.compat.client.gui.TextColors.legacy(color);
        recording.defer(poseStack, (pose, collector) -> collector.submitText(pose, x, y, text, shadow, mode, light,
                argb, backgroundColor, 0));
    }
}
