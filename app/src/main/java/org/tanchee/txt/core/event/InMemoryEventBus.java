package org.tanchee.txt.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InMemoryEventBus implements EventBus {

    private final Map<Class<?>, List<EventHandler<?>>> handlers = new ConcurrentHashMap<>();

    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public <E extends Event> void publish(E event) {
        List<EventHandler<?>> subscribers =
            handlers.getOrDefault(event.getClass(), List.of());
        subscribers.stream()
            .forEach(h -> {
                @SuppressWarnings("unchecked")
                EventHandler<E> handler =  (EventHandler<E>) h;
                CompletableFuture.runAsync(() -> {
                    handler.handle(event).toCompletableFuture().join();
                }, executor);
            });
    }

    @Override
    public <E extends Event> void subscribe(Class<E> type, EventHandler<E> handler) {
        handlers.computeIfAbsent(
            type,
            ignored -> new CopyOnWriteArrayList<>()
        ).add(handler);
    }
}
