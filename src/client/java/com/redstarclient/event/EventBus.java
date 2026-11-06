// ☭ Red Star Client — EventBus
package com.redstarclient.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight, thread-safe publish/subscribe event bus.
 */
public class EventBus {

    /** Functional listener interface. */
    @FunctionalInterface
    public interface EventListener<T> {
        void handle(T event);
    }

    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    /** Subscribe a listener for a specific event type. */
    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /** Unsubscribe a listener. */
    public void unsubscribe(Class<?> eventType, EventListener<?> listener) {
        List<EventListener<?>> list = listeners.get(eventType);
        if (list != null) list.remove(listener);
    }

    /** Post an event to all registered listeners. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void post(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list == null) return;
        for (EventListener listener : list) {
            try {
                listener.handle(event);
            } catch (Exception e) {
                System.err.println("[Red Star Client] EventBus error: " + e.getMessage());
            }
        }
    }
}
