package com.gregtechceu.gtceu.compat.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's per-stack item hooks. Injected into every {@link net.minecraft.world.item.Item} through
 * {@link com.gregtechceu.gtceu.core.fabric.ItemExt}; the ones GT relies on are called from mixins.
 */
public interface IItemExtension {

    default ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    default boolean onDroppedByPlayer(ItemStack item, Player player) {
        return true;
    }

    default Component getHighlightTip(ItemStack item, Component displayName) {
        return displayName;
    }

    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default void onStopUsing(ItemStack stack, LivingEntity entity, int count) {}

    default boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return false;
    }

    /** Remainder left in the crafting grid; defaults to the vanilla remainder. */
    default ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        var remainder = itemStack.getItem().getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    default boolean hasCraftingRemainingItem(ItemStack stack) {
        return !getCraftingRemainingItem(stack).isEmpty();
    }

    default boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return false;
    }

    default boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    default @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return null;
    }

    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    default boolean onEntitySwing(ItemStack stack, LivingEntity entity, net.minecraft.world.InteractionHand hand) {
        return false;
    }

    default int getEnchantmentValue(ItemStack stack) {
        var enchantable = stack.get(DataComponents.ENCHANTABLE);
        return enchantable == null ? 0 : enchantable.value();
    }

    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack);
    }

    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !(newStack.is(oldStack.getItem()) && ItemStack.isSameItemSameComponents(newStack, oldStack));
    }

    default boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return ItemStack.isSameItem(oldStack, newStack);
    }

    default boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return false;
    }

    default boolean canGrindstoneRepair(ItemStack stack) {
        return false;
    }

    default boolean canFitInsideContainerItems(ItemStack stack) {
        return true;
    }

    default int getEntityLifespan(ItemStack itemStack, Level level) {
        return 6000;
    }

    default net.minecraft.world.item.enchantment.ItemEnchantments getAllEnchantments(ItemStack stack,
                                                                                      net.minecraft.core.HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> lookup) {
        return stack.getEnchantments();
    }

    default int getEnchantmentLevel(ItemStack stack,
                                    net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return stack.getEnchantments().getLevel(enchantment);
    }

    default boolean supportsEnchantment(ItemStack stack,
                                        net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return enchantment.value().isSupportedItem(stack);
    }

    default boolean isPrimaryItemFor(ItemStack stack,
                                     net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return enchantment.value().isPrimaryItem(stack);
    }

    default boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return false;
    }

    default int getMaxDamage(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    default int getDamage(ItemStack stack) {
        return stack.getOrDefault(DataComponents.DAMAGE, 0);
    }

    default void setDamage(ItemStack stack, int damage) {
        stack.set(DataComponents.DAMAGE, Math.max(0, damage));
    }

    default boolean isDamaged(ItemStack stack) {
        return getDamage(stack) > 0;
    }

    default boolean isDamageable(ItemStack stack) {
        return stack.has(DataComponents.MAX_DAMAGE);
    }

    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity,
                                                    java.util.function.Consumer<net.minecraft.world.item.Item> onBroken) {
        return amount;
    }

    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return false;
    }

    default @Nullable net.minecraft.world.food.FoodProperties getFoodProperties(ItemStack stack,
                                                                                @Nullable LivingEntity entity) {
        return stack.get(DataComponents.FOOD);
    }

    default int getBurnTime(ItemStack itemStack,
                            @Nullable net.minecraft.world.item.crafting.RecipeType<?> recipeType) {
        return -1;
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == armorType;
    }

    default net.minecraft.sounds.SoundEvent getEatingSound() {
        return net.minecraft.sounds.SoundEvents.GENERIC_EAT.value();
    }

    default net.minecraft.sounds.SoundEvent getDrinkingSound() {
        return net.minecraft.sounds.SoundEvents.GENERIC_DRINK.value();
    }
}
