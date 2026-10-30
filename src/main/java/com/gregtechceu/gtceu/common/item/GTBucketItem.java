package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.fluids.GTFluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import com.gregtechceu.gtceu.compat.client.extensions.common.IClientFluidTypeExtensions;

import org.jetbrains.annotations.Nullable;

public class GTBucketItem extends BucketItem {

    public GTBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
        // 26.x fuels are an item component, not a getBurnTime override
        if (fluid instanceof GTFluid gtFluid) {
            com.gregtechceu.gtceu.compat.registries.datamaps.builtin.NeoForgeDataMaps.setBurnTime(this,
                    gtFluid.getBurnTime());
        }
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return IClientFluidTypeExtensions.of(item.content).getTintColor();
            }
        }
        return -1;
    }

    public Component getDescription() {
        return Component.translatable("item.gtceu.bucket", this.content.getFluidType().getDescription());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }
}
