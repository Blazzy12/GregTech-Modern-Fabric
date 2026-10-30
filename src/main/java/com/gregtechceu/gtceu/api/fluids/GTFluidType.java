package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import com.gregtechceu.gtceu.compat.fluids.FluidStack;
import com.gregtechceu.gtceu.compat.fluids.FluidType;

public class GTFluidType extends FluidType {

    private final Material material;

    public GTFluidType(Properties properties, Material material) {
        super(properties);
        this.material = material;
    }

    @Override
    public String getDescriptionId() {
        // override getDescriptionId() to return the material's name plainly so someone misusing these methods won't
        // show "%s"/"%s Gas"/whatever to the player.
        return material.getUnlocalizedName();
    }

        public Component getDescription() {
        // super.getDescriptionId() returns the GT fluid builder's translation key ("%s Gas", "Molten %s", etc.)
        return Component.translatable(super.getDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getDescription(FluidStack stack) {
        return this.getDescription();
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        FluidStorage fluidStorage = material.getProperty(PropertyKey.FLUID);
        // always vaporize plasmas and gases
        Fluid plasma = fluidStorage.get(FluidStorageKeys.PLASMA);
        if (plasma != null) {
            return !plasma.defaultFluidState().createLegacyBlock().isAir();
        }
        Fluid gas = fluidStorage.get(FluidStorageKeys.GAS);
        if (gas != null) {
            return !gas.defaultFluidState().createLegacyBlock().isAir();
        }

        return false;
    }
}
