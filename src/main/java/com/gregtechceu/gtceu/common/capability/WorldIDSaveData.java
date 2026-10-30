package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTSavedData;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldIDSaveData extends GTSavedData {

    private static WorldIDSaveData instance;
    private static final String DATA_NAME = "gtceu_world_id";

    private String worldID;

    @SuppressWarnings("unused")
    public WorldIDSaveData(ServerLevel level) {
        worldID = level.getServer().getWorldData().getLevelName() + "_" + UUID.randomUUID();
        this.setDirty();
    }

    public WorldIDSaveData(CompoundTag tag) {
        this.worldID = tag.getStringOr("id", "");
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putString("id", worldID);
        return compoundTag;
    }

    public static void init(ServerLevel level) {
        instance = level.getDataStorage()
                .computeIfAbsent(GTSavedData.type(DATA_NAME, () -> new WorldIDSaveData(level), (tag, provider) -> new WorldIDSaveData(tag)));
    }

    public static String getWorldID() {
        return instance.worldID;
    }
}
