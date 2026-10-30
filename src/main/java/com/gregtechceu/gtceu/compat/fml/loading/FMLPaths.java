package com.gregtechceu.gtceu.compat.fml.loading;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.function.Supplier;

public enum FMLPaths {

    GAMEDIR(() -> FabricLoader.getInstance().getGameDir()),
    CONFIGDIR(() -> FabricLoader.getInstance().getConfigDir());

    private final Supplier<Path> path;

    FMLPaths(Supplier<Path> path) {
        this.path = path;
    }

    public Path get() {
        return path.get();
    }
}
