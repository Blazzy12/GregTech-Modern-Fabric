package com.gregtechceu.gtceu.compat.fml.event.lifecycle;

import com.gregtechceu.gtceu.compat.bus.Event;
import com.gregtechceu.gtceu.compat.fml.event.IModBusEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FMLCommonSetupEvent extends Event implements IModBusEvent {

    /** Fabric initialises mods on the main thread, so enqueued work runs immediately. */
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return CompletableFuture.completedFuture(null);
    }

    public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
        return CompletableFuture.completedFuture(work.get());
    }
}
