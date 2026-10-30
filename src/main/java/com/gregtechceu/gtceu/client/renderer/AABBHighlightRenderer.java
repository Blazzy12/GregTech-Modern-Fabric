package com.gregtechceu.gtceu.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

import net.minecraft.world.phys.shapes.Shapes;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.client.Minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.util.List;

@Environment(EnvType.CLIENT)
@NoArgsConstructor
public class AABBHighlightRenderer {

    public static final AABBHighlightRenderer INSTANCE = new AABBHighlightRenderer();

    private final List<AABBHighlight> highlights = new ObjectArrayList<>();

    /** Registers the render hook. */
    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(INSTANCE::render);
    }

    private void render(LevelRenderContext context) {
        if (highlights.isEmpty()) return;
        Vec3 offset = Minecraft.getInstance().gameRenderer.mainCamera().position().reverse();
        PoseStack stack = context.poseStack();
        stack.pushPose();
        stack.translate(offset.x, offset.y, offset.z);
        long time = System.currentTimeMillis();
        // copy, as finished highlights remove themselves
        for (AABBHighlight highlight : List.copyOf(highlights)) {
            highlight.render(stack, context.submitNodeCollector(), time);
        }
        stack.popPose();
    }

    public void addHighlight(AABBHighlight highlight) {
        highlights.add(highlight);
    }

    public record AABBHighlight(AABB aabb, int colorARGB,
                                long startMillis, long durationMillis, long phaseMillis,
                                double thickness) {

        public void render(PoseStack poseStack, SubmitNodeCollector collector, long currentTimeMillis) {
            if (currentTimeMillis - startMillis >= durationMillis) {
                this.remove();
                return;
            }
            if (currentTimeMillis < startMillis) return;
            if ((currentTimeMillis - startMillis) / phaseMillis % 2 == 1) return;

            // drawn through blocks, like before
            collector.submitShapeOutline(poseStack, Shapes.create(aabb), RenderTypes.linesTranslucentNoDepthWrite(),
                    colorARGB, 2f, true);
        }

        public void remove() {
            AABBHighlightRenderer.INSTANCE.highlights.remove(this);
        }
    }

    public static AABBHighlightBuilder builder() {
        return new AABBHighlightBuilder();
    }

    @Accessors(chain = true, fluent = true)
    public static class AABBHighlightBuilder {

        @Setter
        private AABB aabb = null;
        @Setter
        private int colorARGB = 0xFFFFFFFF;
        @Setter
        private long startMillis = -1;
        @Setter
        private long durationMillis = 10000;
        @Setter
        private long phaseMillis = 300;
        @Setter
        private double thickness = 0.01D;

        @Tolerate
        public AABBHighlightBuilder aabb(BlockPos pos) {
            this.aabb = new AABB(pos);
            return this;
        }

        public AABBHighlightBuilder colorARGB(int alpha, int red, int green, int blue) {
            this.colorARGB = ARGB.color(alpha, red, green, blue);
            return this;
        }

        public AABBHighlight build() {
            if (aabb == null) {
                throw new IllegalArgumentException("AABB can't be null in AABBHighlightBuilder");
            }
            if (startMillis == -1) {
                startMillis = System.currentTimeMillis();
            }
            return new AABBHighlight(aabb, colorARGB, startMillis, durationMillis, phaseMillis, thickness);
        }
    }
}
