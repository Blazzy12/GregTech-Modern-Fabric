package com.gregtechceu.gtceu.api.events;

import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import lombok.Getter;

public class ModifyMachineEvent extends Event implements IModBusEvent {

    @Getter
    private final MachineBuilder<?, ?, ?> builder;

    public ModifyMachineEvent(MachineBuilder<?, ?, ?> builder) {
        this.builder = builder;
    }
}
