package com.gregtechceu.gtceu.compat.registries;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import net.minecraft.core.Registry;

/** Registries built with {@link RegistryBuilder} are already added to the root registry on Fabric. */
public class NewRegistryEvent extends Event implements IModBusEvent {

    public <T> void register(Registry<T> registry) {}
}
