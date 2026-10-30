package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.event.RegisterCommandsEvent;
import com.gregtechceu.gtceu.compat.event.entity.EntityJoinLevelEvent;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingDamageEvent;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingDeathEvent;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingEquipmentChangeEvent;
import com.gregtechceu.gtceu.compat.event.entity.player.PlayerEvent;
import com.gregtechceu.gtceu.compat.event.entity.player.PlayerInteractEvent;
import com.gregtechceu.gtceu.compat.event.level.BlockEvent;
import com.gregtechceu.gtceu.compat.common.extensions.IBlockEntityExtension;
import com.gregtechceu.gtceu.compat.event.level.ChunkEvent;
import com.gregtechceu.gtceu.compat.event.level.LevelEvent;
import com.gregtechceu.gtceu.compat.event.server.ServerStartedEvent;
import com.gregtechceu.gtceu.compat.event.server.ServerStartingEvent;
import com.gregtechceu.gtceu.compat.event.server.ServerStoppedEvent;
import com.gregtechceu.gtceu.compat.event.server.ServerStoppingEvent;
import com.gregtechceu.gtceu.compat.event.tick.LevelTickEvent;
import com.gregtechceu.gtceu.compat.event.tick.ServerTickEvent;
import com.gregtechceu.gtceu.compat.fml.ModBuses;

import net.minecraft.world.InteractionResult;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

/**
 * Posts GregTech's (NeoForge-shaped) game events from Fabric callbacks. Events Fabric has no callback for are posted
 * by the mixins in {@code core.mixins.fabric.events}.
 */
public final class FabricEventBridge {

    private FabricEventBridge() {}

    private static <T extends Event> T post(T event) {
        return NeoForge.EVENT_BUS.post(event);
    }

    /**
     * Registers the {@code @EventBusSubscriber} classes (there is no annotation scan on Fabric) on both buses; each
     * of their handlers only receives the events of the bus they are posted on.
     */
    public static void registerSubscribers(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            NeoForge.EVENT_BUS.register(clazz);
            ModBuses.MOD_BUS.register(clazz);
        }
    }

    public static void init() {
        // NeoForge's starting event comes once the levels are loaded, which Fabric's starting event precedes
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            post(new ServerStartingEvent(server));
            post(new ServerStartedEvent(server));
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> post(new ServerStoppingEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> post(new ServerStoppedEvent(server)));

        ServerTickEvents.START_SERVER_TICK.register(server -> post(new ServerTickEvent.Pre(server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> post(new ServerTickEvent.Post(server)));
        ServerTickEvents.START_LEVEL_TICK.register(level -> post(new LevelTickEvent.Pre(level)));
        ServerTickEvents.END_LEVEL_TICK.register(level -> post(new LevelTickEvent.Post(level)));

        ServerLevelEvents.LOAD.register((server, level) -> post(new LevelEvent.Load(level)));
        ServerLevelEvents.UNLOAD.register((server, level) -> post(new LevelEvent.Unload(level)));
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) ->
                post(new ChunkEvent.Load(chunk, level, generated)));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> post(new ChunkEvent.Unload(chunk, level)));

        // NeoForge's block entity hooks: onLoad when one joins a level (placed or loaded with its chunk),
        // onChunkUnloaded when its chunk unloads
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof IBlockEntityExtension extension) extension.onLoad();
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (var blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof IBlockEntityExtension extension) extension.onChunkUnloaded();
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) ->
                post(new EntityJoinLevelEvent(entity, level, false)));
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, from, to) ->
                post(new LivingEquipmentChangeEvent(entity, slot, from, to)));

        ServerPlayerEvents.JOIN.register(player -> post(new PlayerEvent.PlayerLoggedInEvent(player)));
        ServerPlayerEvents.LEAVE.register(player -> post(new PlayerEvent.PlayerLoggedOutEvent(player)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                post(new PlayerEvent.PlayerRespawnEvent(newPlayer, alive)));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
                post(new PlayerEvent.Clone(newPlayer, oldPlayer, !alive)));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, from, to) ->
                post(new PlayerEvent.PlayerChangedDimensionEvent(player, from.dimension(), to.dimension())));

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
                !post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled());
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                post(new PlayerInteractEvent.LeftClickBlock(player, hand, pos, direction,
                        PlayerInteractEvent.Action.START)).isCanceled() ?
                                InteractionResult.SUCCESS : InteractionResult.PASS);
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                post(new PlayerInteractEvent.EntityInteract(player, hand, entity.blockPosition(), null, entity))
                        .isCanceled() ? InteractionResult.SUCCESS : InteractionResult.PASS);

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) ->
                post(new LivingDamageEvent.Post(entity, source, damageTaken, baseDamage)));
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) ->
                !post(new LivingDeathEvent(entity, source)).isCanceled());

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
                post(new RegisterCommandsEvent(dispatcher, selection, context)));
    }
}
