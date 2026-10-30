package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;
import net.minecraft.core.Direction;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IDynamicCoverRenderer {

    void render(MetaMachine machine, Direction face, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay);
}
