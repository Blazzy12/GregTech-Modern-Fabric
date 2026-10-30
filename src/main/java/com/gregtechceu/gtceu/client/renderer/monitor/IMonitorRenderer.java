package com.gregtechceu.gtceu.client.renderer.monitor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IMonitorRenderer {

    @Environment(EnvType.CLIENT)
    void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay);
}
