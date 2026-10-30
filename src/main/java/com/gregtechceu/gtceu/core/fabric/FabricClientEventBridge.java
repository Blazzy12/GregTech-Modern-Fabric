package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.common.extensions.IBlockEntityExtension;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import com.gregtechceu.gtceu.compat.client.event.AddAttributeTooltipsEvent;
import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.entity.player.ItemTooltipEvent;
import com.gregtechceu.gtceu.compat.event.level.LevelEvent;
import com.gregtechceu.gtceu.compat.event.tick.LevelTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.jetbrains.annotations.Nullable;

/** Posts GregTech's client-side game events from Fabric callbacks. */
public final class FabricClientEventBridge {

    private static @Nullable ClientLevel lastLevel;

    private FabricClientEventBridge() {}

    public static void init() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof IBlockEntityExtension extension) extension.onLoad();
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (var blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof IBlockEntityExtension extension) extension.onChunkUnloaded();
            }
        });
        ClientTickEvents.START_LEVEL_TICK.register(level -> NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level)));
        ClientTickEvents.END_LEVEL_TICK.register(level -> NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level)));

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> {
            if (lastLevel != null) NeoForge.EVENT_BUS.post(new LevelEvent.Unload(lastLevel));
            lastLevel = level;
            NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
        });

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            var player = Minecraft.getInstance().player;
            NeoForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, lines::add,
                    new AddAttributeTooltipsEvent.Context(context, player, flag,
                            stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT))));
            NeoForge.EVENT_BUS.post(new ItemTooltipEvent(stack, player, lines, flag, context));
        });
    }
}
