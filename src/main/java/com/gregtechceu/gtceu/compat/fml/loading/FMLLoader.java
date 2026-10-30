package com.gregtechceu.gtceu.compat.fml.loading;

import net.fabricmc.loader.api.FabricLoader;

public final class FMLLoader {

    private FMLLoader() {}

    /** In dev, {@code -Dgtceu.prodLike=true} makes GT and ModularUI behave as in production (high tier off etc.). */
    public static boolean isProduction() {
        return !FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("gtceu.prodLike");
    }

    public static LoadingModList getLoadingModList() {
        return LoadingModList.INSTANCE;
    }

    public static final class LoadingModList {

        static final LoadingModList INSTANCE = new LoadingModList();

        /** Returns a non-null marker when the mod is present. */
        public Object getModFileById(String modId) {
            return FabricLoader.getInstance().getModContainer(modId).orElse(null);
        }

        public boolean hasErrors() {
            return false;
        }
    }
}
