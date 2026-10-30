package com.gregtechceu.gtceu.compat.items;

import com.gregtechceu.gtceu.compat.items.wrapper.PlayerMainInvWrapper;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public final class ItemHandlerHelper {

    private ItemHandlerHelper() {}

    public static ItemStack insertItem(@Nullable IItemHandler dest, ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty()) return stack;
        for (int i = 0; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }

    /** Inserts into slots already holding the same item first, then into empty slots. */
    public static ItemStack insertItemStacked(@Nullable IItemHandler inventory, ItemStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty()) return stack;
        if (!stack.isStackable()) return insertItem(inventory, stack, simulate);

        int sizeInventory = inventory.getSlots();
        for (int i = 0; i < sizeInventory; i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(slot, stack)) {
                stack = inventory.insertItem(i, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        for (int i = 0; i < sizeInventory; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                stack = inventory.insertItem(i, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    public static void giveItemToPlayer(Player player, ItemStack stack) {
        giveItemToPlayer(player, stack, -1);
    }

    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (stack.isEmpty()) return;
        IItemHandler inventory = new PlayerMainInvWrapper(player.getInventory());
        Level level = player.level();

        ItemStack remainder = stack;
        if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
            remainder = inventory.insertItem(preferredSlot, stack, false);
        }
        if (!remainder.isEmpty()) remainder = insertItemStacked(inventory, remainder, false);

        if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
            level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS, 0.2F,
                    ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        if (!remainder.isEmpty() && !level.isClientSide()) {
            ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
            entity.setPickUpDelay(40);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
            level.addFreshEntity(entity);
        }
    }
}
