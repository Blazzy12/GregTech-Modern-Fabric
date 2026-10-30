package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import com.gregtechceu.gtceu.compat.bus.IEventBus;
import com.gregtechceu.gtceu.compat.bus.SubscribeEvent;

import com.tterrag.registrate.util.entry.FluidEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTFluids {

    @SuppressWarnings("UnstableApiUsage")
    public static final FluidEntry<PotionFluid> POTION = REGISTRATE
            .fluid("potion", GTCEu.id("block/fluids/fluid.potion"), GTCEu.id("block/fluids/fluid.potion"),
                    PotionFluid.PotionFluidType::new, PotionFluid::new)
            .lang("Potion")
            .source(PotionFluid::new).noBlock().noBucket()
            .tag(CustomTags.POTION_FLUIDS)
            .register();

    public static void init(IEventBus modBus) {
        // Register fluids for non-materials
        modBus.register(GTFluids.class);
    }

    @SubscribeEvent
    public static void modifyMaterials(PostMaterialEvent event) {
        handleNonMaterialFluids(GTMaterials.Water.value(), Fluids.WATER);
        handleNonMaterialFluids(GTMaterials.Lava.value(), Fluids.LAVA);
        // no shared milk fluid on Fabric: GT registers its own from the Milk material
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Fluid fluid) {
        handleNonMaterialFluids(material, () -> fluid);
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Supplier<Fluid> fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, fluid, null);
    }

    public static void registerMaterialFluids() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_FLUID);
        for (var material : GTRegistries.MATERIALS) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);

            if (fluidProperty != null) {
                fluidProperty.registerFluids(material, GTRegistrate.createIgnoringListenerErrors(material.getModid()));
            }
        }
    }
}
