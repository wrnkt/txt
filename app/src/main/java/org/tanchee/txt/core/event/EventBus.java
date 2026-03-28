package org.tanchee.txt.core.event;

public interface EventBus {
    <E extends Event> void publish(E event);
    <E extends Event> void subscribe(Class<E> type, EventHandler<E> handler);
}
