package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import org.jetbrains.annotations.Nullable;

/**
 * Replacement for the removed {@code ArmorItem}: an item worn in an armor slot, configured through
 * {@link Item.Properties#humanoidArmor}.
 */
public class GTArmorBaseItem extends Item {

    protected final ArmorMaterial material;
    protected final ArmorType type;

    public GTArmorBaseItem(ArmorMaterial material, ArmorType type, Properties properties) {
        super(armorProperties(properties, material, type));
        this.material = material;
        this.type = type;
    }

    /** {@link Item.Properties#humanoidArmor}, minus enchantability for materials without it (GT's powered armor). */
    private static Properties armorProperties(Properties properties, ArmorMaterial material, ArmorType type) {
        if (material.enchantmentValue() > 0) {
            return properties.humanoidArmor(material, type);
        }
        return properties.durability(type.getDurability(material.durability()))
                .attributes(material.createAttributes(type))
                .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                        net.minecraft.world.item.equipment.Equippable.builder(type.getSlot())
                                .setEquipSound(material.equipSound()).setAsset(material.assetId()).build())
                .repairable(material.repairIngredient());
    }

    public ArmorType getType() {
        return type;
    }

    public EquipmentSlot getEquipmentSlot() {
        return type.getSlot();
    }

    public ArmorMaterial getMaterial() {
        return material;
    }

    public int getDefense() {
        return material.defense().getOrDefault(type, 0);
    }

    public float getToughness() {
        return material.toughness();
    }

    /**
     * The texture rendered when worn, or null for the default derived from the material's asset id.
     */
    public @Nullable Identifier getArmorTexture(ItemStack stack, @Nullable Entity entity, EquipmentSlot slot,
                                                ArmorLayer layer, boolean innerModel) {
        return null;
    }

    public static ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> assetKey(Identifier id) {
        return ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, id);
    }
}
