package com.gregtechceu.gtceu.compat.network.connection;

public enum ConnectionType {

    OTHER,
    NEOFORGE;

    public boolean isOther() {
        return this == OTHER;
    }

    public boolean isNeoForge() {
        return this == NEOFORGE;
    }
}
