package org.tanchee.txt.core.state;

import java.util.Map;
import java.util.Optional;

import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.monitoring.ComponentHealth;

public interface ComponentStateStore {
    ComponentState getState(ComponentId id);
    void setState(ComponentId id, ComponentState state);

    ComponentHealth getHealth(ComponentId id);
    void setHealth(ComponentId id, ComponentHealth health);

    <T> Optional<T> get(ComponentId id, String key, Class<T> type);

    void put(ComponentId id, String key, Object value);
    void remove(ComponentId id, String key);

    Map<String, Object> snapshow(ComponentId id);
}
