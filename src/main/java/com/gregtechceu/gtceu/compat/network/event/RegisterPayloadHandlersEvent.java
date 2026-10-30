package com.gregtechceu.gtceu.compat.network.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;
import com.gregtechceu.gtceu.compat.network.registration.PayloadRegistrar;

public class RegisterPayloadHandlersEvent extends Event implements IModBusEvent {

    public PayloadRegistrar registrar(String version) {
        return new PayloadRegistrar(version);
    }
}
