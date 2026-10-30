package com.gregtechceu.gtceu.api.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.client.model.runtimegen.TagPrefixItemModelGenerator;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import com.gregtechceu.gtceu.compat.client.color.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TagPrefixItem extends Item {

    public final TagPrefix tagPrefix;
    public final Material material;

    public TagPrefixItem(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties);
        this.tagPrefix = tagPrefix;
        this.material = material;
        // 26.x fuels are an item component, not a getBurnTime override
        com.gregtechceu.gtceu.compat.registries.datamaps.builtin.NeoForgeDataMaps.setBurnTime(this, getItemBurnTime());
        if (GTCEu.isClientSide()) {
            TagPrefixItemModelGenerator.add(this, tagPrefix.materialIconType(), material.getMaterialIconSet());
        }
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag flag) {
        List<Component> lines = new java.util.ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(tooltipAdder);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (this.tagPrefix.tooltip() != null) {
            this.tagPrefix.tooltip().accept(material, tooltipComponents);
        }
    }
    /** Translation key; {@code getDescriptionId()} is final in 26.x. */
    public String getGTDescriptionId() {
        return tagPrefix.getUnlocalizedName(material);
    }

        public Component getDescription() {
        return tagPrefix.getLocalizedName(material);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity,
                              @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        // 26.x only ticks on the server and passes the equipment slot instead of an inventory index
        int slotId = slot == null ? -1 : slot.getIndex();
        boolean isSelected = slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND;
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.tickCount % 20 == 0) {
                if (tagPrefix != TagPrefix.ingotHot.value() || !material.hasProperty(PropertyKey.BLAST))
                    return;

                float heatDamage = ((material.getBlastTemperature() - 1750) / 1000.0F) + 2;
                ItemStack armor = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                if (armor.getItem() instanceof ModularArmorItem modularArmor) {
                    heatDamage *= modularArmor.getHeatResistance();
                } else if (!armor.isEmpty() && armor.getItem() instanceof ArmorComponentItem armorItem &&
                        armorItem.getArmorLogic() != null) {
                            heatDamage *= armorItem.getArmorLogic().getHeatResistance();
                        }
                if (heatDamage > 0.0) {
                    livingEntity.hurt(level.damageSources().source(GTDamageTypes.HEAT), heatDamage);
                } else if (heatDamage < 0.0) {
                    livingEntity.hurt(livingEntity.damageSources().freeze(), -heatDamage);
                }
            }
        }
    }

    public int getItemBurnTime() {
        DustProperty property = material.getProperty(PropertyKey.DUST);
        if (property != null)
            return (int) (property.getBurnTime() * tagPrefix.getMaterialAmount(material) / GTValues.M);
        return 0;
    }

    // TODO BEACON PAYMENT
}
