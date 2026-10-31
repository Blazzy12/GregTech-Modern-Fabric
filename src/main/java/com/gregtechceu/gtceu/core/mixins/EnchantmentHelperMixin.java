package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * GT tools have innate enchantments (e.g. Looting III on the butchery knife) kept in their own component. NeoForge
 * folds them in through {@code IItemExtension#getAllEnchantments}, which vanilla never calls, so they are added
 * wherever {@link EnchantmentHelper} reads an item's enchantments.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyExpressionValue(method = "getItemEnchantmentLevel",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/item/ItemInstance;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object gtceu$innateForLevel(Object enchantments, @Local(argsOnly = true) ItemInstance item) {
        return item instanceof ItemStack stack ? gtceu$withInnate(stack, enchantments) : enchantments;
    }

    @ModifyExpressionValue(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object gtceu$innateForIteration(Object enchantments, @Local(argsOnly = true) ItemStack stack) {
        return gtceu$withInnate(stack, enchantments);
    }

    @ModifyExpressionValue(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private static Object gtceu$innateForSlotIteration(Object enchantments, @Local(argsOnly = true) ItemStack stack) {
        return gtceu$withInnate(stack, enchantments);
    }

    @Unique
    private static Object gtceu$withInnate(ItemStack stack, Object enchantments) {
        if (!(stack.getItem() instanceof IGTTool tool) || !(enchantments == null || enchantments instanceof ItemEnchantments)) {
            return enchantments;
        }
        var access = com.gregtechceu.gtceu.core.fabric.Registries.access();
        if (access == null) return enchantments;
        ItemEnchantments all = tool.definition$getAllEnchantments(stack, access.lookupOrThrow(Registries.ENCHANTMENT));
        return all.isEmpty() ? enchantments : all;
    }
}
