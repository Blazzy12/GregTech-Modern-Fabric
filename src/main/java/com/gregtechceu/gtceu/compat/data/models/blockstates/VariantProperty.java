package com.gregtechceu.gtceu.compat.data.models.blockstates;

import com.google.gson.JsonElement;

import java.util.function.Function;

public record VariantProperty<T>(String key, Function<T, JsonElement> serializer) {

    public Value withValue(T value) {
        return new Value(key, serializer.apply(value));
    }

    public record Value(String key, JsonElement json) {}
}
