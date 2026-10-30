package com.gregtechceu.gtceu.api;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.fabric.Registries;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Saved data stored as a compound tag, as before 26.x. {@link #type} builds the 26.x {@link SavedDataType};
 * the constructor and loader may capture the level, since a type is created per lookup.
 */
public abstract class GTSavedData extends SavedData {

    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider provider);

    public static <T extends GTSavedData> SavedDataType<T> type(String name, Supplier<T> constructor,
                                                                BiFunction<CompoundTag, HolderLookup.Provider, T> loader) {
        String path = name.startsWith(GTCEu.MOD_ID + "_") ? name.substring(GTCEu.MOD_ID.length() + 1) : name;
        return new SavedDataType<>(GTCEu.id(path), constructor,
                CompoundTag.CODEC.xmap(tag -> loader.apply(tag, Registries.access()),
                        data -> data.save(new CompoundTag(), Registries.access())),
                null);
    }
}
