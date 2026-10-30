package com.gregtechceu.gtceu.client.renderer.machine.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import net.minecraft.client.Minecraft;
import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;

import static net.minecraft.util.ARGB.*;

public class FusionRingRender extends DynamicRender<FusionReactorMachine, FusionRingRender> {

    // spotless:off
    public static final MapCodec<FusionRingRender> CODEC = MapCodec.unit(FusionRingRender::new);
    public static final DynamicRenderType<FusionReactorMachine, FusionRingRender> TYPE = new DynamicRenderType<>(FusionRingRender.CODEC);
    // spotless:on

    public static final float FADEOUT = 60;

    public FusionRingRender() {}

    @Override
    public DynamicRenderType<FusionReactorMachine, FusionRingRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(FusionReactorMachine machine, Vec3 cameraPos) {
        return (machine.recipeLogic.isWorking() || machine.delta > 0) && super.shouldRender(machine, cameraPos);
    }

    @Override
    public void render(FusionReactorMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.recipeLogic.isWorking() && machine.delta <= 0) {
            return;
        }

        // without the bloom blur, the ring drawn here is all there is; no separate bloom render is registered
        renderLightRing(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.lightRing()));
    }

    @Environment(EnvType.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks,
                                 PoseStack stack, VertexConsumer buffer) {
        float alpha = 1f;
        if (machine.recipeLogic.isWorking()) {
            machine.lastColor = machine.getColor();
            machine.delta = FADEOUT;
        } else {
            alpha = machine.delta / FADEOUT;
            machine.lastColor = color(Mth.floor(alpha * 255), red(machine.lastColor), green(machine.lastColor),
                    blue(machine.lastColor));
            machine.delta -= Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        }

        final var lerpFactor = Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25;
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        var r = Mth.lerp(lerpFactor, red(machine.lastColor), 255) / 255f;
        var g = Mth.lerp(lerpFactor, green(machine.lastColor), 255) / 255f;
        var b = Mth.lerp(lerpFactor, blue(machine.lastColor), 255) / 255f;
        RenderBufferHelper.renderRing(stack, buffer,
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, alpha, axis);
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorMachine machine) {
        return machine.recipeLogic.isWorking() || machine.delta > 0;
    }

    @Override
    public AABB getRenderBoundingBox(FusionReactorMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(getViewDistance() / 2.0D);
    }
}
