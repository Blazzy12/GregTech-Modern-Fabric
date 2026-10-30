package com.gregtechceu.gtceu.compat.fml;

import com.gregtechceu.gtceu.compat.bus.EventBus;
import com.gregtechceu.gtceu.compat.bus.IEventBus;

public final class ModBuses {

    /** Replacement for the per-mod NeoForge bus; GT and its addons share it. */
    public static final IEventBus MOD_BUS = new EventBus("mod");

    private ModBuses() {}
}
