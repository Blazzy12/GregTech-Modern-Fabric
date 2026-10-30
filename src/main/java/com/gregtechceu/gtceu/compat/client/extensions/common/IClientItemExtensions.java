package com.gregtechceu.gtceu.compat.client.extensions.common;

import net.minecraft.world.item.Item;

/** Client-side item hooks. 26.x item rendering is model driven, so only the registration survives. */
public interface IClientItemExtensions {

    /** Tint for one armour texture layer; read by GT's equipment layer rendering. */
    default int getArmorLayerTintColor(net.minecraft.world.item.ItemStack stack,
                                       net.minecraft.world.entity.LivingEntity entity,
                                       com.gregtechceu.gtceu.api.item.armor.ArmorLayer layer, int layerIdx,
                                       int fallbackColor) {
        return fallbackColor;
    }

    IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

    static IClientItemExtensions of(Item item) {
        return ClientExtensionsManager.ITEM_EXTENSIONS.getOrDefault(item, DEFAULT);
    }

    static IClientItemExtensions of(net.minecraft.world.item.ItemStack stack) {
        return of(stack.getItem());
    }

    /** The model used to render this item when worn; GT's armor renderer uses it. */
    default net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(
                                                                              net.minecraft.world.entity.LivingEntity livingEntity,
                                                                              net.minecraft.world.item.ItemStack itemStack,
                                                                              net.minecraft.world.entity.EquipmentSlot equipmentSlot,
                                                                              net.minecraft.client.model.HumanoidModel<?> original) {
        return original;
    }
}
