package com.gregtechceu.gtceu.compat.data.models.blockstates;

import net.minecraft.world.level.block.Block;

import com.google.gson.JsonElement;

import java.util.function.Supplier;

/** A blockstate definition rendered to JSON. Mirrors the 1.21.1 vanilla datagen API GT generates from. */
public interface BlockStateGenerator extends Supplier<JsonElement> {

    Block getBlock();
}
