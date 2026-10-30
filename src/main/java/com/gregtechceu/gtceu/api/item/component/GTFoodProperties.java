package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Food definition in the pre-26.x shape. 26.x splits food into {@link FoodProperties} (nutrition) and a
 * {@link Consumable} (eat time, animation, effects); this keeps both together so GT items can be declared in one go.
 *
 * @param food           nutrition values
 * @param effects        status effects applied on eating, with their chance
 * @param consumeSeconds how long eating takes
 */
public record GTFoodProperties(FoodProperties food, List<Effect> effects, float consumeSeconds) {

    public static final float DEFAULT_CONSUME_SECONDS = 1.6F;
    public static final float FAST_CONSUME_SECONDS = 0.8F;

    public record Effect(Supplier<MobEffectInstance> supplier, float probability) {

        public MobEffectInstance effect() {
            return supplier.get();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** The consumable component; built on demand since effects may reference registry contents. */
    public Consumable consumable(boolean drink) {
        Consumable.Builder builder = (drink ? Consumables.defaultDrink() : Consumables.defaultFood())
                .consumeSeconds(consumeSeconds);
        for (Effect effect : effects) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect.effect(), effect.probability()));
        }
        return builder.build();
    }

    /** Applies this food to item properties, replacing {@code Item.Properties#food(FoodProperties)}. */
    public Item.Properties apply(Item.Properties properties, boolean drink) {
        return properties.food(food, consumable(drink));
    }

    public static class Builder {

        private final FoodProperties.Builder food = new FoodProperties.Builder();
        private final List<Effect> effects = new ArrayList<>();
        private float consumeSeconds = DEFAULT_CONSUME_SECONDS;

        public Builder nutrition(int nutrition) {
            food.nutrition(nutrition);
            return this;
        }

        public Builder saturationModifier(float saturationModifier) {
            food.saturationModifier(saturationModifier);
            return this;
        }

        public Builder alwaysEdible() {
            food.alwaysEdible();
            return this;
        }

        public Builder fast() {
            consumeSeconds = FAST_CONSUME_SECONDS;
            return this;
        }

        public Builder consumeSeconds(float seconds) {
            consumeSeconds = seconds;
            return this;
        }

        public Builder effect(Supplier<MobEffectInstance> effect, float probability) {
            effects.add(new Effect(effect, probability));
            return this;
        }

        public GTFoodProperties build() {
            return new GTFoodProperties(food.build(), List.copyOf(effects), consumeSeconds);
        }
    }
}
