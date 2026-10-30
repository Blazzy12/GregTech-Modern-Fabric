package com.gregtechceu.gtceu.compat.common.crafting;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

import java.util.IdentityHashMap;
import java.util.Map;

public record IngredientType<T extends ICustomIngredient>(MapCodec<T> codec,
                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    private static final Map<IngredientType<?>, CustomIngredientSerializer<?>> SERIALIZERS = new IdentityHashMap<>();

    public IngredientType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }

    /** Called when the type is added to the ingredient type registry. */
    public static <T extends ICustomIngredient> void bind(Identifier id, IngredientType<T> type) {
        CustomIngredientSerializer<T> serializer = new CustomIngredientSerializer<>() {

            @Override
            public Identifier getIdentifier() {
                return id;
            }

            @Override
            public MapCodec<T> getCodec() {
                return type.codec();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec() {
                return (StreamCodec<RegistryFriendlyByteBuf, T>) type.streamCodec();
            }
        };
        SERIALIZERS.put(type, serializer);
        CustomIngredientSerializer.register(serializer);
    }

    static CustomIngredientSerializer<?> serializerFor(IngredientType<?> type) {
        CustomIngredientSerializer<?> serializer = SERIALIZERS.get(type);
        if (serializer == null) throw new IllegalStateException("Ingredient type " + type + " is not registered");
        return serializer;
    }
}
