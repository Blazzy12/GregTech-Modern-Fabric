package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class HydraulicArmorItem extends ModularArmorItem {

    public HydraulicArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(material, type, properties);
    }

    @Override
    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      com.gregtechceu.gtceu.api.item.armor.ArmorLayer layer, boolean innerModel) {
        var texture = super.getArmorTexture(stack, entity, slot, layer, innerModel);
        if (texture != null) return texture;

        String armorTexture = "hydraulic_armor_suite";
        return slot != EquipmentSlot.LEGS ?
                GTCEu.id(String.format("textures/armor/%s_1.png", armorTexture)) :
                GTCEu.id(String.format("textures/armor/%s_2.png", armorTexture));
    }
}
