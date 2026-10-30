package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.valueprovider.AddedFloat;
import com.gregtechceu.gtceu.common.valueprovider.CastedFloat;
import com.gregtechceu.gtceu.common.valueprovider.FlooredInt;
import com.gregtechceu.gtceu.common.valueprovider.MultipliedFloat;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GTValueProviderTypes {

    public static void init() {
        Registry.register(BuiltInRegistries.INT_PROVIDER_TYPE, GTCEu.id("floored"), FlooredInt.CODEC);
        Registry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, GTCEu.id("multiplied"), MultipliedFloat.CODEC);
        Registry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, GTCEu.id("added"), AddedFloat.CODEC);
        Registry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, GTCEu.id("casted"), CastedFloat.CODEC);
    }
}
