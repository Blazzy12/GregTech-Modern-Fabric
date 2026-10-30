package com.gregtechceu.gtceu.data.pack.event;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import org.jetbrains.annotations.ApiStatus;

public class RegisterDynamicResourcesEvent extends Event implements IModBusEvent {

    @ApiStatus.Internal
    public RegisterDynamicResourcesEvent() {}
}
