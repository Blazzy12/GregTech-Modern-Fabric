/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gregtechceu.gtceu.compat.client.model.generators;

import com.google.common.base.Preconditions;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;

public abstract class ModelFile {
    protected Identifier location;

    protected ModelFile(Identifier location) {
        this.location = location;
    }

    protected abstract boolean exists();

    public Identifier getLocation() {
        assertExistence();
        return location;
    }

    /**
     * Assert that this model exists.
     * 
     * @throws IllegalStateException if this model does not exist
     */
    public void assertExistence() {
        Preconditions.checkState(exists(), "Model at %s does not exist", location);
    }

    public Identifier getUncheckedLocation() {
        return location;
    }

    public static class UncheckedModelFile extends ModelFile {
        public UncheckedModelFile(String location) {
            this(Identifier.parse(location));
        }

        public UncheckedModelFile(Identifier location) {
            super(location);
        }

        @Override
        protected boolean exists() {
            return true;
        }
    }

    public static class ExistingModelFile extends ModelFile {
        private final ExistingFileHelper existingHelper;

        public ExistingModelFile(Identifier location, ExistingFileHelper existingHelper) {
            super(location);
            this.existingHelper = existingHelper;
        }

        @Override
        protected boolean exists() {
            if (getUncheckedLocation().getPath().contains("."))
                return existingHelper.exists(getUncheckedLocation(), ModelProvider.MODEL_WITH_EXTENSION);
            else
                return existingHelper.exists(getUncheckedLocation(), ModelProvider.MODEL);
        }
    }
}
