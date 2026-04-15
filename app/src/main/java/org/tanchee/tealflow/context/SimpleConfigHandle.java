package org.tanchee.tealflow.context;

import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.config.ConfigHandle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Map-backed {@link ConfigHandle} for test and local-runner use.
 *
 * <p>Config objects are registered by {@link ComponentId} before the context is
 * started. {@link #get(ComponentId, Class)} performs a type-safe cast; if no
 * config is registered for the given id an {@link IllegalStateException} is thrown.
 *
 * <p>Change notifications ({@link #onChange}) are not supported and are silently ignored.
 *
 * <p>Usage:
 * <pre>{@code
 * SimpleConfigHandle config = new SimpleConfigHandle()
 *     .register(collectorId,  EpubCollectorConfig.of(path))
 *     .register(chunkerId,    ChunkingConfig.defaults());
 * }</pre>
 */
public class SimpleConfigHandle implements ConfigHandle {

    private final Map<ComponentId, Object> configs = new HashMap<>();

    /**
     * Registers a config object for the given component id.
     * Returns {@code this} for fluent chaining.
     */
    public <T> SimpleConfigHandle register(ComponentId id, T config) {
        configs.put(id, config);
        return this;
    }

    @Override
    public <T> T get(ComponentId id, Class<T> type) {
        Object value = configs.get(id);
        if (value == null) {
            throw new IllegalStateException(
                "No config registered for component '" + id.value() + "'. "
                + "Call SimpleConfigHandle.register() before starting the context.");
        }
        return type.cast(value);
    }

    @Override
    public <T> void onChange(Class<T> configType, Consumer<T> listener) {
        // Not implemented — dynamic config changes are out of scope for simple context.
    }
}
