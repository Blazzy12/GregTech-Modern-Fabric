package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.common.NeoForge;
import com.gregtechceu.gtceu.compat.common.util.TriState;
import com.gregtechceu.gtceu.compat.event.entity.living.LivingEntityUseItemEvent;
import com.gregtechceu.gtceu.compat.event.entity.player.PlayerDestroyItemEvent;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/** Helpers that NeoForge uses to fire events from its patched vanilla code. */
public final class EventHooks {

    private EventHooks() {}

    public static ItemStack onItemUseFinish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
        var event = new LivingEntityUseItemEvent.Finish(entity, item, duration, result);
        NeoForge.EVENT_BUS.post(event);
        return event.getResultStack();
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {
        NeoForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack, hand));
    }

    /** @return true when the explosion was cancelled */
    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return false;
    }

    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {}

    public static PickupResult fireItemPickupPre(ItemEntity itemEntity, Player player) {
        return new PickupResult(TriState.DEFAULT);
    }

    public static void fireItemPickupPost(ItemEntity itemEntity, Player player, ItemStack copy) {}

    public record PickupResult(TriState canPickup) {}
}
