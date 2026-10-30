package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ArmorLayer;
import com.gregtechceu.gtceu.api.item.armor.GTArmorBaseItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GTArmorMaterials {

    public static final List<ArmorLayer> BLANK_LAYERS = List.of(new ArmorLayer(GTCEu.id("armor")));

    /** GT armor repairs through its own logic, never in an anvil. */
    private static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, GTCEu.id("no_repair"));

    public static final ArmorMaterial GOGGLES = material("goggles", 0, 0, 0, 0, 0, 0.0F, 0.0F);
    public static final ArmorMaterial JETPACK = material("jetpack", 0, 0, 0, 0, 0, 0.0F, 0.0F);
    public static final ArmorMaterial BAD_PPE_EQUIPMENT = material("bad_ppe_equipment", 1, 2, 3, 1, 10, 0.0F, 0.0F);
    public static final ArmorMaterial GOOD_PPE_EQUIPMENT = material("good_ppe_equipment", 2, 5, 6, 2, 10, 0.0F, 0.0F);
    public static final ArmorMaterial HYDRAULIC = material("hydraulic", 3, 6, 8, 3, 50, 2.0F, 0.0F);
    public static final ArmorMaterial NANO_MUSCLE = material("nano_muscle", 4, 8, 12, 4, 50, 4.0F, 0.0F);
    public static final ArmorMaterial QUARK_TECH = material("quark_tech", 6, 12, 18, 6, 50, 5.0F, 0.0F);

    private static ArmorMaterial material(String name, int boots, int leggings, int chestplate, int helmet,
                                          int enchantability, float toughness, float knockbackResistance) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, boots);
        defense.put(ArmorType.LEGGINGS, leggings);
        defense.put(ArmorType.CHESTPLATE, chestplate);
        defense.put(ArmorType.HELMET, helmet);
        return new ArmorMaterial(0, defense, enchantability, SoundEvents.ARMOR_EQUIP_GENERIC, toughness,
                knockbackResistance, NO_REPAIR, GTArmorBaseItem.assetKey(GTCEu.id(name)));
    }

    public static void init() {}
}
