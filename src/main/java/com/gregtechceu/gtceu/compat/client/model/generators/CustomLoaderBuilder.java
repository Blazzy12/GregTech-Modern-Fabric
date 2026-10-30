/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gregtechceu.gtceu.compat.client.model.generators;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.client.model.geometry.IGeometryLoader;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;

public abstract class CustomLoaderBuilder<T extends ModelBuilder<T>> {
    protected final Identifier loaderId;
    protected final T parent;
    protected final ExistingFileHelper existingFileHelper;
    protected final Map<String, Boolean> visibility = new LinkedHashMap<>();
    protected final boolean allowInlineElements;
    private boolean optional = false;

    /**
     * @param loaderId            The ID of the associated {@link IGeometryLoader}
     * @param parent              The parent {@link ModelBuilder}
     * @param existingFileHelper  The {@link ExistingFileHelper}
     * @param allowInlineElements Whether the loader supports inline vanilla elements and as such can fall back to vanilla loading
     *                            with some degradation if the loader does not exist and is marked as optional in the model
     */
    protected CustomLoaderBuilder(Identifier loaderId, T parent, ExistingFileHelper existingFileHelper, boolean allowInlineElements) {
        this.loaderId = loaderId;
        this.parent = parent;
        this.existingFileHelper = existingFileHelper;
        this.allowInlineElements = allowInlineElements;
    }

    public CustomLoaderBuilder<T> visibility(String partName, boolean show) {
        Preconditions.checkNotNull(partName, "partName must not be null");
        this.visibility.put(partName, show);
        return this;
    }

    /**
     * Mark the custom loader as optional for this model to allow it to be loaded through vanilla paths
     * if the loader is not present
     */
    public CustomLoaderBuilder<T> optional() {
        Preconditions.checkState(allowInlineElements, "Only loaders with support for inline elements can be marked as optional");
        this.optional = true;
        return this;
    }

    public T end() {
        return parent;
    }

    public JsonObject toJson(JsonObject json) {
        // Fabric names custom model types with "fabric:type"; NeoForge's own loaders have GregTech ports
        Identifier type = loaderId.getNamespace().equals("neoforge") ?
                Identifier.fromNamespaceAndPath("gtceu", loaderId.getPath()) : loaderId;
        if (optional) {
            JsonObject loaderObj = new JsonObject();
            loaderObj.addProperty("id", type.toString());
            loaderObj.addProperty("optional", true);
            json.add("fabric:type", loaderObj);
        } else {
            json.addProperty("fabric:type", type.toString());
        }

        if (visibility.size() > 0) {
            JsonObject visibilityObj = new JsonObject();

            for (Map.Entry<String, Boolean> entry : visibility.entrySet()) {
                visibilityObj.addProperty(entry.getKey(), entry.getValue());
            }

            json.add("visibility", visibilityObj);
        }

        return json;
    }
}
