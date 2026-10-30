package com.gregtechceu.gtceu.compat.fml.util.thread;

import com.gregtechceu.gtceu.compat.fml.LogicalSide;

public final class EffectiveSide {

    private EffectiveSide() {}

    /** The integrated and dedicated server threads are named "Server thread". */
    public static LogicalSide get() {
        String name = Thread.currentThread().getName();
        return name.equals("Server thread") || name.startsWith("Netty Server") ? LogicalSide.SERVER :
                LogicalSide.CLIENT;
    }
}
