/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gregtechceu.gtceu.compat.client.model.generators;

import net.minecraft.data.PackOutput;
import com.gregtechceu.gtceu.compat.common.data.ExistingFileHelper;

/**
 * Stub class to extend for block model data providers, eliminates some
 * boilerplate constructor parameters.
 */
public abstract class BlockModelProvider extends ModelProvider<BlockModelBuilder> {
    public BlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, BLOCK_FOLDER, BlockModelBuilder::new, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Block Models: " + modid;
    }
}
