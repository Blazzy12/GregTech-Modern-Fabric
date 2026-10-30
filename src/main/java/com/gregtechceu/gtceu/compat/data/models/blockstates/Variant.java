package com.gregtechceu.gtceu.compat.data.models.blockstates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** One model variant of a blockstate, such as {@code {"model": ..., "y": 90}}. */
public class Variant implements Supplier<JsonElement> {

    private final Map<String, JsonElement> values = new LinkedHashMap<>();

    public static Variant variant() {
        return new Variant();
    }

    public static Variant merge(Variant first, Variant second) {
        Variant result = new Variant();
        result.values.putAll(first.values);
        result.values.putAll(second.values);
        return result;
    }

    public <T> Variant with(VariantProperty<T> property, T value) {
        values.put(property.key(), property.serializer().apply(value));
        return this;
    }

    @Override
    public JsonElement get() {
        JsonObject json = new JsonObject();
        values.forEach(json::add);
        return json;
    }

    public static JsonElement convertList(List<Variant> variants) {
        if (variants.size() == 1) return variants.getFirst().get();
        JsonArray array = new JsonArray();
        variants.forEach(v -> array.add(v.get()));
        return array;
    }
}
