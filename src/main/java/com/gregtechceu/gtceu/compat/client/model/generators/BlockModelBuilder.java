/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gregtechceu.gtceu.compat.client.model.generators;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;

/**
 * Builder for block models, does not currently provide any additional
 * functionality over {@link ModelBuilder}, purely a stub class with a concrete
 * generic.
 *
 * @see ModelProvider
 * @see ModelBuilder
 */
public class BlockModelBuilder extends ModelBuilder<BlockModelBuilder> {
    public BlockModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    @Override
    public JsonObject toJson() {
        return super.toJson();
    }
}
