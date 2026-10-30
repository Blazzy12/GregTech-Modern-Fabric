package com.gregtechceu.gtceu.compat.dist;

/**
 * Physical side marker, kept for source compatibility with code written against NeoForge.
 */
public enum Dist {

    CLIENT,
    DEDICATED_SERVER;

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isDedicatedServer() {
        return this == DEDICATED_SERVER;
    }
}
