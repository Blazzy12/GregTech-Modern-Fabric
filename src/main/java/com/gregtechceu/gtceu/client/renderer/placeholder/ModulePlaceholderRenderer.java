package com.gregtechceu.gtceu.client.renderer.placeholder;

import com.gregtechceu.gtceu.core.fabric.ItemStackNbt;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.placeholder.IPlaceholderRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import com.gregtechceu.gtceu.compat.client.render.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class ModulePlaceholderRenderer implements IPlaceholderRenderer {

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay, CompoundTag tag) {
        ItemStack stack = ItemStackNbt.parse(machine.getLevel().registryAccess(), tag).orElse(ItemStack.EMPTY);
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof IMonitorModuleItem module) {
                    module.getRenderer(stack, machine, group).render(machine, group, partialTick, poseStack, buffer,
                            packedLight,
                            packedOverlay);
                }
            }
        }
    }
}
