package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Simple {@link IEdibleItem} implementation.
 * <p>
 * Eating is driven by the {@code food} and {@code consumable} item components in 26.x; {@code ComponentItem} adds them
 * to the item's default components when this is attached.
 */
public class FoodStats implements IEdibleItem, IInteractionItem, IAddInformation {

    protected final GTFoodProperties properties;

    protected final boolean isDrink;
    @Nullable
    protected final Supplier<ItemStack> containerItem;

    public FoodStats(GTFoodProperties properties, boolean isDrink, boolean hasPotionEffects,
                     @Nullable Supplier<ItemStack> containerItem) {
        this(properties, isDrink, containerItem);
    }

    public FoodStats(GTFoodProperties properties, boolean isDrink, @Nullable Supplier<ItemStack> containerItem) {
        this.properties = properties;
        this.isDrink = isDrink;
        this.containerItem = containerItem;
    }

    public FoodStats(GTFoodProperties properties) {
        this(properties, false, null);
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return properties.food();
    }

    public Consumable getConsumable() {
        return properties.consumable(isDrink);
    }

    @Override
    public SoundEvent getEatingSound() {
        return isDrink ? getDrinkingSound() : IEdibleItem.super.getEatingSound();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        GTUtil.addPotionTooltip(properties.effects(), tooltipComponents);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack food, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;
        Consumable consumable = food.get(DataComponents.CONSUMABLE);
        var stack = consumable != null ? consumable.onConsume(level, livingEntity, food) : food;
        if (containerItem != null && (player == null || !player.getAbilities().instabuild)) {
            var container = containerItem.get();
            if (stack.isEmpty()) {
                return container;
            }

            if (player != null) {
                if (!player.getInventory().add(container)) {
                    player.drop(container, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            }
        }
        return stack;
    }
}
