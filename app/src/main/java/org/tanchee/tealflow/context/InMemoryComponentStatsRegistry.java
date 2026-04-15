package org.tanchee.tealflow.context;

import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.monitoring.ComponentStats;
import org.tanchee.txt.core.monitoring.ComponentStatsRegistry;

import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ComponentStatsRegistry} backed by a {@link ConcurrentHashMap}.
 *
 * <p>A fresh {@link ComponentStats} instance is created on first access for any
 * component id. Thread-safe via {@code computeIfAbsent}.
 */
public class InMemoryComponentStatsRegistry implements ComponentStatsRegistry {

    private final ConcurrentHashMap<ComponentId, ComponentStats> registry =
        new ConcurrentHashMap<>();

    @Override
    public ComponentStats get(ComponentId id) {
        return registry.computeIfAbsent(id, ignored -> new ComponentStats());
    }
}
