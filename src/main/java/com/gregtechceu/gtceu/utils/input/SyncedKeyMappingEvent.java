package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

/**
 * Event to register {@link SyncedKeyMapping}s in.
 * <br>
 * Event is fired on the mod bus.
 */
public class SyncedKeyMappingEvent extends Event implements IModBusEvent {

    public SyncedKeyMappingEvent() {
        super();
    }
}
