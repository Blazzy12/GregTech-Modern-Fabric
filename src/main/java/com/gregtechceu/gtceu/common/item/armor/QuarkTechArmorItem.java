package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class QuarkTechArmorItem extends ModularArmorItem {

    public QuarkTechArmorItem(ArmorMaterial material, net.minecraft.world.item.equipment.ArmorType type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean protectsAgainstFalls() {
        return getType() == net.minecraft.world.item.equipment.ArmorType.BOOTS;
    }

    @Override
    public float getHeatResistance() {
        return getType() == net.minecraft.world.item.equipment.ArmorType.CHESTPLATE ? 0.5f : 1.0f;
    }

    @Override
    public boolean protectsAgainstFire() {
        return getType() == net.minecraft.world.item.equipment.ArmorType.CHESTPLATE;
    }

    @Override
    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      com.gregtechceu.gtceu.api.item.armor.ArmorLayer layer, boolean innerModel) {
        var texture = super.getArmorTexture(stack, entity, slot, layer, innerModel);
        if (texture != null) return texture;

        String armorTexture = "quark_tech_suite";
        return slot != EquipmentSlot.LEGS ?
                GTCEu.id(String.format("textures/armor/%s_1.png", armorTexture)) :
                GTCEu.id(String.format("textures/armor/%s_2.png", armorTexture));
    }
}
