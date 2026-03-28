package org.tanchee.txt.core.state;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.health.ComponentHealth;
import org.tanchee.txt.core.health.HealthStatus;

public class InMemoryComponentStateStore implements ComponentStateStore {

    private final Map<ComponentId, ComponentState> states = new ConcurrentHashMap<>();
    private final Map<ComponentId, ComponentHealth> health = new ConcurrentHashMap<>();
    private final Map<ComponentId, Map<String, Object>> values = new ConcurrentHashMap<>();

    @Override
    public ComponentState getState(ComponentId id) {
        return states.getOrDefault(id, ComponentState.CREATED);
    }

    @Override
    public void setState(ComponentId id, ComponentState state) {
        states.put(id, state);
    }

    @Override
    public ComponentHealth getHealth(ComponentId id) {
        return health.getOrDefault(
            id, 
            new ComponentHealth(
                HealthStatus.UNKNOWN,
                "No health info",
                Instant.now(),
                Map.of()
            )
        );
    }

    @Override
    public void setHealth(ComponentId id, ComponentHealth value) {
        health.put(id, value);
    }

    @Override
    public <T> Optional<T> get(ComponentId id, String key, Class<T> type) {
        Object value = values
            .getOrDefault(id, Map.of())
            .get(key);

        if (null == value) 
            return Optional.empty();

        return Optional.of(type.cast(value));
    }

    @Override
    public void put(ComponentId id, String key, Object value) {
        values.computeIfAbsent(id, ignored -> new ConcurrentHashMap<>())
            .put(key, value);
    }

    @Override
    public void remove(ComponentId id, String key) {
        values.computeIfAbsent(id, ignored -> new ConcurrentHashMap<>())
            .remove(key);
    }

    @Override
    public Map<String, Object> snapshow(ComponentId id) {
        return Map.copyOf(
            values.getOrDefault(id, Map.of())
        );
    }
}
