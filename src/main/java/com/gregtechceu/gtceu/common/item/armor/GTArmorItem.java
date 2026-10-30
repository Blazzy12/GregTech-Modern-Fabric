package com.gregtechceu.gtceu.common.item.armor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.model.runtimegen.ArmorItemModelGenerator;

import com.gregtechceu.gtceu.compat.client.color.ItemColor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class GTArmorItem extends com.gregtechceu.gtceu.api.item.armor.GTArmorBaseItem {

    public final Material material;
    public final ArmorProperty armorProperty;

    public GTArmorItem(ArmorType type, Properties properties, Material material, ArmorProperty armorProperty) {
        super(armorProperty.getArmorMaterial(), type, properties);
        this.material = material;
        this.armorProperty = armorProperty;
        if (GTCEu.isClientSide()) {
            ArmorItemModelGenerator.add(this, type);
        }
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }
    /** Translation key; {@code getDescriptionId()} is final in 26.x. */
    public String getGTDescriptionId() {
        String matSpecificKey = String.format("item.%s.%s_%s",
                material.getModid(), material.getName(), type.getName());
        if (Language.getInstance().has(matSpecificKey)) {
            return matSpecificKey;
        }
        return "item.gtceu.armor." + type.getName();
    }

        public Component getDescription() {
        return Component.translatable(getGTDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      com.gregtechceu.gtceu.api.item.armor.ArmorLayer layer, boolean innerModel) {
        return armorProperty.getCustomTextureGetter().getCustomTexture(stack, entity, slot, layer);
    }
}
