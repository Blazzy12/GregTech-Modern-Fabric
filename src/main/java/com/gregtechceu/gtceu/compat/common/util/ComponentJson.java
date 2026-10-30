package com.gregtechceu.gtceu.compat.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

/** {@code Component.Serializer.toJson/fromJson}, which 26.x removed in favour of {@link ComponentSerialization}. */
public final class ComponentJson {

    private static final Gson GSON = new Gson();

    private ComponentJson() {}

    public static String toJson(Component component, HolderLookup.Provider registries) {
        JsonElement json = ComponentSerialization.CODEC
                .encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), component).getOrThrow();
        return GSON.toJson(json);
    }

    public static @Nullable MutableComponent fromJson(String json, HolderLookup.Provider registries) {
        JsonElement element = JsonParser.parseString(json);
        return ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), element)
                .result().map(Component::copy).orElse(null);
    }
}
