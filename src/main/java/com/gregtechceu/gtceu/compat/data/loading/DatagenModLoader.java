package com.gregtechceu.gtceu.compat.data.loading;

/** NeoForge datagen never runs on Fabric. */
public final class DatagenModLoader {

    private DatagenModLoader() {}

    public static boolean isRunningDataGen() {
        return false;
    }
}
