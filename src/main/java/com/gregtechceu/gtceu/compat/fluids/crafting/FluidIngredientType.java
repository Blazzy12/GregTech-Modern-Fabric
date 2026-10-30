package com.gregtechceu.gtceu.compat.fluids.crafting;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

public record FluidIngredientType<T extends FluidIngredient>(MapCodec<T> codec,
                                                             StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    public static final FluidIngredientType<EmptyFluidIngredient> EMPTY = new FluidIngredientType<>(
            EmptyFluidIngredient.CODEC);
    public static final FluidIngredientType<SingleFluidIngredient> SINGLE = new FluidIngredientType<>(
            SingleFluidIngredient.CODEC);
    public static final FluidIngredientType<TagFluidIngredient> TAG = new FluidIngredientType<>(
            TagFluidIngredient.CODEC);
    public static final FluidIngredientType<CompoundFluidIngredient> COMPOUND = new FluidIngredientType<>(
            CompoundFluidIngredient.CODEC);
    public static final FluidIngredientType<DataComponentFluidIngredient> DATA_COMPONENT = new FluidIngredientType<>(
            DataComponentFluidIngredient.CODEC);
    public static final FluidIngredientType<IntersectionFluidIngredient> INTERSECTION = new FluidIngredientType<>(
            IntersectionFluidIngredient.CODEC);
    public static final FluidIngredientType<DifferenceFluidIngredient> DIFFERENCE = new FluidIngredientType<>(
            DifferenceFluidIngredient.CODEC);

    public FluidIngredientType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }

    public static void registerBuiltins(Registry<FluidIngredientType<?>> registry) {
        register(registry, "empty", EMPTY);
        register(registry, "single", SINGLE);
        register(registry, "tag", TAG);
        register(registry, "compound", COMPOUND);
        register(registry, "components", DATA_COMPONENT);
        register(registry, "intersection", INTERSECTION);
        register(registry, "difference", DIFFERENCE);
    }

    private static void register(Registry<FluidIngredientType<?>> registry, String name,
                                 FluidIngredientType<?> type) {
        Registry.register(registry, Identifier.fromNamespaceAndPath("neoforge", name), type);
    }
}
