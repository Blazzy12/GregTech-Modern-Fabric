/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.gregtechceu.gtceu.compat.client.model.generators;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public interface IGeneratedBlockState {
    JsonObject toJson();
}
