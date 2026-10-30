package com.gregtechceu.gtceu.core.fabric;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Converts between 26.x value input/output and the flat {@link CompoundTag}s GT's sync system works with.
 * Keys are written individually so saves keep the same layout as before.
 */
public final class ValueIO {

    private ValueIO() {}

    public static CompoundTag read(ValueInput input) {
        CompoundTag tag = new CompoundTag();
        for (String key : input.keySet()) {
            input.read(key, ExtraCodecs.NBT).ifPresent(value -> tag.put(key, value));
        }
        return tag;
    }

    public static void write(ValueOutput output, CompoundTag tag) {
        for (String key : tag.keySet()) {
            var value = tag.get(key);
            if (value != null) output.store(key, ExtraCodecs.NBT, value);
        }
    }
}
