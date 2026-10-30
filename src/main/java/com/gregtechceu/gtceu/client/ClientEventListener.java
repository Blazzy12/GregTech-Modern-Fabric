package com.gregtechceu.gtceu.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.chunk.LevelChunk;

import net.minecraft.world.level.Level;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.AABBHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.BlockHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.commands.GTClientCommands;
import com.gregtechceu.gtceu.common.data.GTAttributeModifierIds;
import com.gregtechceu.gtceu.common.data.GTMobEffects;
import com.gregtechceu.gtceu.common.module.MovementSpeedItemModule;
import com.gregtechceu.gtceu.common.module.SpeedItemModule;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientEventListener {

    /**
     * @return the movement speed that should widen the FOV: GregTech's speed boosts (armor modules, block speed
     *         boosts) don't, so they don't make the view swim
     */
    public static double getFovMovementSpeed(AttributeInstance attribute) {
        var modifiers = attribute.getModifiers().stream().filter(modifier -> {
            if (modifier.id().equals(GTAttributeModifierIds.BLOCK_SPEED_BOOST)) return false;
            var module = GTRegistries.ITEM_MODULES.getValue(modifier.id());
            return !(module instanceof SpeedItemModule || module instanceof MovementSpeedItemModule);
        }).toList();
        double base = attribute.getBaseValue();
        for (AttributeModifier mod : modifiers) {
            if (mod.operation() == AttributeModifier.Operation.ADD_VALUE) base += mod.amount();
        }

        double applied = base;
        for (AttributeModifier mod : modifiers) {
            if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) applied += base * mod.amount();
        }

        for (AttributeModifier mod : modifiers) {
            if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) applied *= 1 + mod.amount();
        }

        return attribute.getAttribute().value().sanitizeValue(applied);
    }

    /** Registers GregTech's client event hooks. */
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TooltipHelper.onClientTick();
            EnvironmentalHazardClientHandler.INSTANCE.onClientTick();
            GTValues.CLIENT_TIME++;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FacadeCoverRenderer.clearItemModelCache();
            ClientCacheManager.allowReinit();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ClientCacheManager.clearCaches());
        DebugScreenEntries.register(GTCEu.id("machine"), new MachineDebugEntry());
        PatternPreviewRenderer.init();
        AABBHighlightRenderer.init();
        BlockHighlightRenderer.init();
    }

    private static final String BLOCK_INFO_LINE_START = ChatFormatting.UNDERLINE + "Targeted Block: ";

    /** The state of the machine looked at, in the debug screen. */
    private static final class MachineDebugEntry implements DebugScreenEntry {

        @Override
        public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk,
                            @Nullable LevelChunk serverChunk) {
            List<String> lines = new ArrayList<>();
            addMachineLines(lines);
            if (!lines.isEmpty()) displayer.addToGroup(GTCEu.id("machine"), lines);
        }
    }

    private static void addMachineLines(List<String> rightLines) {
        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null || mc.level == null) return;

        BlockHitResult hit = ToolHelper.entityPickBlock(cameraEntity, 20.0, 0, false);
        if (hit.getType() == HitResult.Type.MISS) return;
        BlockPos hitPos = hit.getBlockPos();
        BlockEntity blockEntity = mc.level.getBlockEntity(hitPos);
        // only try to find the correct location if we have a valid machine
        if (!(blockEntity instanceof MetaMachine machineBE)) return;

        machineBE.addDebugOverlayText(rightLines::add);
    }
}
