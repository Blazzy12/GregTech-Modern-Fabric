package com.gregtechceu.gtceu.compat.common;

import com.gregtechceu.gtceu.compat.event.level.BlockEvent;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public final class CommonHooks {

    private static final ThreadLocal<Player> CRAFTING_PLAYER = new ThreadLocal<>();

    private CommonHooks() {}

    /**
     * NeoForge's {@code IBlockStateExtension#getToolModifiedState}, answered from 26.x's data-driven block
     * transformers ({@code minecraft:axe}, {@code shovel}, {@code hoe}). Axe abilities are told apart by the
     * transform's particle: none for stripping, scrape, and wax off. Never changes the world.
     *
     * @return the state the block would turn into, or {@code null}
     */
    public static @Nullable BlockState getToolModifiedState(BlockState state, net.minecraft.world.item.context.UseOnContext context,
                                                            ItemAbility ability, boolean simulate) {
        net.minecraft.resources.ResourceKey<net.minecraft.core.component.BlockTransformer> key;
        net.minecraft.core.component.BlockTransformer.TransformParticle particle = null;
        if (ability == ItemAbilities.AXE_STRIP) {
            key = net.minecraft.world.item.component.BlockTransformers.AXE;
            particle = net.minecraft.core.component.BlockTransformer.TransformParticle.NONE;
        } else if (ability == ItemAbilities.AXE_SCRAPE) {
            key = net.minecraft.world.item.component.BlockTransformers.AXE;
            particle = net.minecraft.core.component.BlockTransformer.TransformParticle.SCRAPE;
        } else if (ability == ItemAbilities.AXE_WAX_OFF) {
            key = net.minecraft.world.item.component.BlockTransformers.AXE;
            particle = net.minecraft.core.component.BlockTransformer.TransformParticle.WAX_OFF;
        } else if (ability == ItemAbilities.SHOVEL_FLATTEN) {
            key = net.minecraft.world.item.component.BlockTransformers.SHOVEL;
        } else if (ability == ItemAbilities.HOE_TILL) {
            key = net.minecraft.world.item.component.BlockTransformers.HOE;
        } else {
            return null;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        var transformer = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK_TRANSFORMER)
                .get(key);
        if (transformer.isEmpty()) return null;
        for (var data : transformer.get().value().transforms()) {
            if (particle != null && data.particle() != particle) continue;
            if (data.disallowedFaces().contains(context.getClickedFace())) continue;
            // providers read the block at pos, so evaluate against the given state's position
            BlockState result = data.blockStateProvider().value().getOptionalState(level, level.getRandom(), pos);
            if (result != null) return result;
        }
        return null;
    }

    public static void setCraftingPlayer(@Nullable Player player) {
        CRAFTING_PLAYER.set(player);
    }

    public static @Nullable Player getCraftingPlayer() {
        return CRAFTING_PLAYER.get();
    }

    /**
     * Checks whether a player may break a block, running Fabric's break callbacks so protection mods apply.
     * The returned event is cancelled when the break is not allowed.
     */
    public static BlockEvent.BreakEvent fireBlockBreak(Level level, GameType gameType, ServerPlayer player,
                                                       BlockPos pos, BlockState state) {
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        boolean preCancelled = player.blockActionRestricted(level, pos, gameType);
        if (preCancelled || !PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state,
                level.getBlockEntity(pos))) {
            event.setCanceled(true);
        }
        NeoForge.EVENT_BUS.post(event);
        return event;
    }
}
