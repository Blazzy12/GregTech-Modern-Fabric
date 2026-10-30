package com.gregtechceu.gtceu.compat.fml;

import com.gregtechceu.gtceu.compat.spi.language.IModInfo;

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.Optional;

public final class ModList {

    private static final ModList INSTANCE = new ModList();

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public Optional<ModContainer> getModContainerById(String modId) {
        return Optional.ofNullable(ModContainer.of(modId));
    }

    public List<IModInfo> getMods() {
        return FabricLoader.getInstance().getAllMods().stream().map(IModInfo::new).toList();
    }
}
