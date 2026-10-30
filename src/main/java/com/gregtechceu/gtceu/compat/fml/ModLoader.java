package com.gregtechceu.gtceu.compat.fml;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

public final class ModLoader {

    private ModLoader() {}

    public static <T extends Event & IModBusEvent> void postEvent(T event) {
        ModBuses.MOD_BUS.post(event);
    }

    public static <T extends Event & IModBusEvent> void postEventWrapContainerInModOrder(T event) {
        ModBuses.MOD_BUS.post(event);
    }

    public static boolean hasErrors() {
        return false;
    }
}
