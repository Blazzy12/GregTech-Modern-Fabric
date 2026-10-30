package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTSavedData;

import com.gregtechceu.gtceu.api.placeholder.Placeholder;

import com.gregtechceu.gtceu.compat.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlaceholderSavedData extends GTSavedData {

    private final ServerLevel level;
    private final CompoundTag tag;

    public static PlaceholderSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(GTSavedData.type("gtceu_placeholder_data", () -> new PlaceholderSavedData(level), (tag, provider) -> new PlaceholderSavedData(level, tag, provider)));
    }

    public PlaceholderSavedData(ServerLevel level) {
        this(level, new CompoundTag(), (HolderLookup.Provider) null);
    }

    public PlaceholderSavedData(ServerLevel level, CompoundTag tag, @Nullable HolderLookup.Provider provider) {
        this.level = level;
        this.tag = tag.getCompoundOrEmpty("data");
    }

    public CompoundTag getPlaceholderData(Placeholder placeholder) {
        if (!tag.contains(placeholder.getName()))
            tag.put(placeholder.getName(), new CompoundTag());
        return tag.getCompoundOrEmpty(placeholder.getName());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("data", this.tag);
        return tag;
    }
}
