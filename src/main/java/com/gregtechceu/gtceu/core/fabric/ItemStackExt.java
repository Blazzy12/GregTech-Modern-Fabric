package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.compat.capabilities.ItemCapability;
import com.gregtechceu.gtceu.compat.common.ItemAbility;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/** Injected into {@link ItemStack}: NeoForge extension methods GT relies on. */
public interface ItemStackExt {

    private ItemStack self() {
        return (ItemStack) this;
    }

    default <T> @Nullable T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    default <T, C> @Nullable T getCapability(ItemCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    default <T> @Nullable T get(java.util.function.Supplier<? extends net.minecraft.core.component.DataComponentType<? extends T>> type) {
        return self().get(type.get());
    }

    default <T> T getOrDefault(java.util.function.Supplier<? extends net.minecraft.core.component.DataComponentType<? extends T>> type,
                               T defaultValue) {
        return self().getOrDefault(type.get(), defaultValue);
    }

    default boolean has(java.util.function.Supplier<? extends net.minecraft.core.component.DataComponentType<?>> type) {
        return self().has(type.get());
    }

    default <T> @Nullable T set(java.util.function.Supplier<? extends net.minecraft.core.component.DataComponentType<T>> type,
                                @Nullable T value) {
        return self().set(type.get(), value);
    }

    default <T> @Nullable T remove(java.util.function.Supplier<? extends net.minecraft.core.component.DataComponentType<? extends T>> type) {
        return self().remove(type.get());
    }

    default <T> @Nullable T update(java.util.function.Supplier<? extends net.minecraft.core.component.DataComponentType<T>> type,
                                   T defaultValue, java.util.function.UnaryOperator<T> updater) {
        return self().update(type.get(), defaultValue, updater);
    }

    default boolean canPerformAction(ItemAbility ability) {
        return ((ItemExt) self().getItem()).canPerformAction(self(), ability);
    }

    default boolean hasCraftingRemainingItem() {
        return ((ItemExt) self().getItem()).hasCraftingRemainingItem(self());
    }

    default ItemStack getCraftingRemainingItem() {
        return ((ItemExt) self().getItem()).getCraftingRemainingItem(self());
    }

    default @Nullable net.minecraft.world.food.FoodProperties getFoodProperties(
                                                                                @Nullable net.minecraft.world.entity.LivingEntity entity) {
        return ((ItemExt) self().getItem()).getFoodProperties(self(), entity);
    }

    default Stream<TagKey<Item>> getTags() {
        return self().typeHolder().tags();
    }

    default Holder<Item> getItemHolder() {
        return self().typeHolder();
    }
}
