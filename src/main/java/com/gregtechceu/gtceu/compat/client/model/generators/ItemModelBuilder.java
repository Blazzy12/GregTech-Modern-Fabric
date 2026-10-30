/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gregtechceu.gtceu.compat.client.model.generators;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;

/**
 * Builder for item models, adds the ability to build overrides via
 * {@link #override()}.
 */
public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {
    protected List<OverrideBuilder> overrides = new ArrayList<>();

    public ItemModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
        // existence checks are off on Fabric (no datagen), so vanilla trim textures need no tracking
    }

    public OverrideBuilder override() {
        OverrideBuilder ret = new OverrideBuilder();
        overrides.add(ret);
        return ret;
    }

    /**
     * Get an existing override builder
     * 
     * @param index the index of the existing override builder
     * @return the override builder
     * @throws IndexOutOfBoundsException if {@code} index is out of bounds
     */
    public OverrideBuilder override(int index) {
        Preconditions.checkElementIndex(index, overrides.size(), "override");
        return overrides.get(index);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        if (!overrides.isEmpty()) {
            JsonArray overridesJson = new JsonArray();
            overrides.stream().map(OverrideBuilder::toJson).forEach(overridesJson::add);
            root.add("overrides", overridesJson);
        }
        return root;
    }

    public class OverrideBuilder {
        private ModelFile model;
        private final Map<Identifier, Float> predicates = new LinkedHashMap<>();

        public OverrideBuilder model(ModelFile model) {
            this.model = model;
            model.assertExistence();
            return this;
        }

        public OverrideBuilder predicate(Identifier key, float value) {
            this.predicates.put(key, value);
            return this;
        }

        public ItemModelBuilder end() {
            return ItemModelBuilder.this;
        }

        JsonObject toJson() {
            JsonObject ret = new JsonObject();
            JsonObject predicatesJson = new JsonObject();
            predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
            ret.add("predicate", predicatesJson);
            ret.addProperty("model", model.getLocation().toString());
            return ret;
        }
    }
}
