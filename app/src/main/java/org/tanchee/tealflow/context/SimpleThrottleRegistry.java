package org.tanchee.tealflow.context;

import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.throttle.ThrottleHandle;
import org.tanchee.txt.core.throttle.ThrottleRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ThrottleRegistry} that returns a registered {@link ThrottleHandle} per component,
 * falling back to {@link PassThroughThrottle} for any component not explicitly configured.
 *
 * <p>Usage:
 * <pre>{@code
 * new SimpleThrottleRegistry()
 *     .register(redditCollectorId, new TokenBucketThrottle(/* tokens per refill *\/))
 * }</pre>
 */
public class SimpleThrottleRegistry implements ThrottleRegistry {

    private final Map<ComponentId, ThrottleHandle> throttles = new ConcurrentHashMap<>();

    /** Registers an explicit throttle for a component. Returns {@code this} for chaining. */
    public SimpleThrottleRegistry register(ComponentId id, ThrottleHandle throttle) {
        throttles.put(id, throttle);
        return this;
    }

    @Override
    public ThrottleHandle get(ComponentId id) {
        return throttles.getOrDefault(id, PassThroughThrottle.INSTANCE);
    }
}
