package com.gregtechceu.gtceu.compat.common;

import com.gregtechceu.gtceu.compat.bus.EventBus;
import com.gregtechceu.gtceu.compat.bus.IEventBus;

public final class NeoForge {

    /** Game events, fed from Fabric callbacks and mixins by the event bridge. */
    public static final IEventBus EVENT_BUS = new EventBus("game");

    private NeoForge() {}
}
