package com.gregtechceu.gtceu.compat.bus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus implements IEventBus {

    /** {@code owner} is what {@link #unregister} matches: the listener object, or the registered target. */
    private record Listener(EventPriority priority, boolean receiveCanceled, Class<?> type,
                            Consumer<Event> consumer, long order, Object owner) {}

    private final String name;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, List<Listener>> cache = new ConcurrentHashMap<>();
    private long counter;

    public EventBus(String name) {
        this.name = name;
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, false, eventType, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> listener) {
        addListener(priority, false, eventType, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled,
                                                           Class<T> eventType, Consumer<T> listener) {
        addListener(priority, receiveCanceled, eventType, (Consumer<Event>) listener, listener);
    }

    private synchronized void addListener(EventPriority priority, boolean receiveCanceled, Class<?> eventType,
                                          Consumer<Event> listener, Object owner) {
        listeners.add(new Listener(priority, receiveCanceled, eventType, listener, counter++, owner));
        cache.clear();
    }

    @Override
    public synchronized void unregister(Object target) {
        if (listeners.removeIf(listener -> listener.owner == target)) {
            cache.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(Object target) {
        boolean isStatic = target instanceof Class<?>;
        Class<?> clazz = isStatic ? (Class<?>) target : target.getClass();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (Method method : clazz.getDeclaredMethods()) {
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null || Modifier.isStatic(method.getModifiers()) != isStatic) continue;
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new IllegalArgumentException("Invalid @SubscribeEvent method " + method + " on bus " + name);
            }
            try {
                method.setAccessible(true);
                MethodHandle handle = lookup.unreflect(method);
                if (!isStatic) handle = handle.bindTo(target);
                MethodHandle bound = handle;
                Class<Event> type = (Class<Event>) method.getParameterTypes()[0];
                addListener(annotation.priority(), annotation.receiveCanceled(), type, (Consumer<Event>) event -> {
                    try {
                        bound.invoke(event);
                    } catch (RuntimeException | Error e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }, target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access event handler " + method, e);
            }
        }
    }

    @Override
    public <T extends Event> T post(T event) {
        List<Listener> matching = cache.computeIfAbsent(event.getClass(), this::collect);
        for (Listener listener : matching) {
            if (event.canceled && !listener.receiveCanceled) continue;
            listener.consumer.accept(event);
        }
        return event;
    }

    private List<Listener> collect(Class<?> eventClass) {
        List<Listener> result = new ArrayList<>();
        for (Listener listener : listeners) {
            if (listener.type.isAssignableFrom(eventClass)) result.add(listener);
        }
        result.sort(Comparator.comparing(Listener::priority).thenComparingLong(Listener::order));
        return result;
    }

    @Override
    public String toString() {
        return "EventBus[" + name + "]";
    }
}
