package com.gregtechceu.gtceu.common.loot.function;

import net.minecraft.core.Holder;
import java.util.Optional;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.utils.codec.GTCodecUtils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.google.gson.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


public class SetEUChargeFunction extends LootItemConditionalFunction {

    // spotless:off
    public static final MapCodec<SetEUChargeFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(
            GTCodecUtils.NON_NEGATIVE_LONG.fieldOf("charge").forGetter((SetEUChargeFunction fn) -> fn.charge)
    ).apply(instance, SetEUChargeFunction::new));
    // spotless:on

    protected final long charge;

    protected SetEUChargeFunction(Optional<Holder<LootItemCondition>> predicates, long charge) {
        super(predicates);
        this.charge = charge;
    }

    @Override
    public MapCodec<SetEUChargeFunction> codec() {
        return CODEC;
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) {
            return stack;
        }
        electricItem.charge(this.charge, Integer.MAX_VALUE, true, false);
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> setCharge(long charge) {
        return simpleBuilder(conditions -> new SetEUChargeFunction(conditions, charge));
    }
}
