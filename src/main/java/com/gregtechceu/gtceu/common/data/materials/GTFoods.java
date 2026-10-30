package com.gregtechceu.gtceu.common.data.materials;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.gregtechceu.gtceu.api.item.component.GTFoodProperties;

public class GTFoods {

    public final static GTFoodProperties CHOCOLATE = GTFoodProperties.builder()
            .effect(() -> new MobEffectInstance(MobEffects.SPEED, 200, 1), 0.1F)
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public final static GTFoodProperties DRINK = GTFoodProperties.builder()
            .effect(() -> new MobEffectInstance(MobEffects.HASTE, 800, 1), 0.9F)
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public static final GTFoodProperties ANTIDOTE = GTFoodProperties.builder()
            .alwaysEdible().fast().build();

    public static void init() {}
}
