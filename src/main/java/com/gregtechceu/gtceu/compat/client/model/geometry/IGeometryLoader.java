package com.gregtechceu.gtceu.compat.client.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

/**
 * Reads a model with a custom loader. Loaders are registered with {@link GeometryLoaderModel#register}; the JSON
 * names them with {@code "fabric:type"}.
 */
@FunctionalInterface
public interface IGeometryLoader<T extends IUnbakedGeometry<T>> {

    @Nullable
    T read(JsonObject json, JsonDeserializationContext context) throws JsonParseException;
}
