package com.gregtechceu.gtceu.api.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import com.gregtechceu.gtceu.compat.client.color.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialBlockItem extends BlockItem {

    public final TagPrefix tagPrefix;
    public final Material material;

    public MaterialBlockItem(Block block, Properties properties, TagPrefix tagPrefix, Material material) {
        super(block, properties);
        this.tagPrefix = tagPrefix;
        this.material = material;
        // 26.x fuels are an item component, not a getBurnTime override
        com.gregtechceu.gtceu.compat.registries.datamaps.builtin.NeoForgeDataMaps.setBurnTime(this, getItemBurnTime());
    }

    @Override
    @NotNull
    public MaterialBlock getBlock() {
        return (MaterialBlock) super.getBlock();
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

        public Component getDescription() {
        return getBlock().getName();
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }

    public int getItemBurnTime() {
        DustProperty property = material.getProperty(PropertyKey.DUST);
        if (property != null) {
            return (int) (property.getBurnTime() * tagPrefix.getMaterialAmount(material) / GTValues.M);
        } else {
            return 0;
        }
    }
}
