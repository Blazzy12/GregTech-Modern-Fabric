package com.gregtechceu.gtceu.compat.bus;

import java.util.function.Consumer;

/**
 * Minimal replacement for the NeoForge event bus. Listeners must name their event class explicitly
 * since there is no generic type resolution for lambdas.
 */
public interface IEventBus {

    <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType,
                                       Consumer<T> listener);

    /**
     * Registers every {@link SubscribeEvent} method of the target. A {@link Class} registers static methods,
     * any other object registers its instance methods.
     */
    void register(Object target);

    <T extends Event> T post(T event);

    /** Removes the listeners added by {@link #register(Object)} for {@code target}, or the given listener object. */
    void unregister(Object target);
}
