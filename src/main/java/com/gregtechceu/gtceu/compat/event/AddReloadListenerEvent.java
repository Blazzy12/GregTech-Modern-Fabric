package com.gregtechceu.gtceu.compat.event;

import com.gregtechceu.gtceu.compat.bus.Event;

import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.List;

/** Collects server data reload listeners. Fired once at startup; listeners are registered with Fabric. */
public class AddReloadListenerEvent extends Event {

    private final List<PreparableReloadListener> listeners;

    public AddReloadListenerEvent(List<PreparableReloadListener> listeners) {
        this.listeners = listeners;
    }

    public void addListener(PreparableReloadListener listener) {
        listeners.add(listener);
    }
}
