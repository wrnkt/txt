package org.tanchee.tealflow.context;

import org.tanchee.txt.core.component.ComponentContext;
import org.tanchee.txt.core.config.ConfigHandle;
import org.tanchee.txt.core.event.EventBus;
import org.tanchee.txt.core.event.InMemoryEventBus;
import org.tanchee.txt.core.monitoring.ComponentStatsRegistry;
import org.tanchee.txt.core.state.ComponentStateStore;
import org.tanchee.txt.core.state.InMemoryComponentStateStore;
import org.tanchee.txt.core.throttle.ThrottleRegistry;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A fully in-memory {@link ComponentContext} suitable for local runners, integration
 * tests, and getting started without any external infrastructure.
 *
 * <p>Wiring:
 * <ul>
 *   <li>Event bus — {@link InMemoryEventBus} (virtual-thread executor, ConcurrentHashMap subscriptions)</li>
 *   <li>Config — caller-supplied {@link SimpleConfigHandle}</li>
 *   <li>Throttles — {@link SimpleThrottleRegistry} (defaults to {@link PassThroughThrottle})</li>
 *   <li>State — {@link InMemoryComponentStateStore}</li>
 *   <li>Stats — {@link InMemoryComponentStatsRegistry}</li>
 *   <li>Executor — {@code newVirtualThreadPerTaskExecutor()} (Java 21+)</li>
 * </ul>
 *
 * <p>To swap the event bus for Inngest (or any other implementation), replace
 * {@link InMemoryEventBus} with an {@code InngestEventBus} instance that implements
 * the same {@link EventBus} interface. No other change is required.
 *
 * <p>To enable per-component rate limiting, call
 * {@link SimpleThrottleRegistry#register(org.tanchee.txt.core.component.ComponentId, org.tanchee.txt.core.throttle.ThrottleHandle)}
 * on {@link #throttleRegistry()} before starting any components.
 */
public class SimpleComponentContext implements ComponentContext {

    private final EventBus              eventBus;
    private final SimpleConfigHandle    config;
    private final SimpleThrottleRegistry throttles;
    private final ComponentStateStore   state;
    private final ComponentStatsRegistry stats;
    private final Executor              executor;

    public SimpleComponentContext(SimpleConfigHandle config) {
        this.eventBus  = new InMemoryEventBus();
        this.config    = config;
        this.throttles = new SimpleThrottleRegistry();
        this.state     = new InMemoryComponentStateStore();
        this.stats     = new InMemoryComponentStatsRegistry();
        this.executor  = Executors.newVirtualThreadPerTaskExecutor();
    }

    // ── ComponentContext ──────────────────────────────────────────────────────

    @Override public EventBus              eventBus()  { return eventBus;  }
    @Override public ConfigHandle          config()    { return config;    }
    @Override public ThrottleRegistry      throttles() { return throttles; }
    @Override public ComponentStateStore   state()     { return state;     }
    @Override public ComponentStatsRegistry stats()    { return stats;     }
    @Override public Executor              executor()  { return executor;  }

    // ── Escape hatches for advanced wiring ───────────────────────────────────

    /**
     * Returns the mutable throttle registry so callers can register per-component
     * {@link org.tanchee.txt.core.throttle.TokenBucketThrottle} instances after
     * construction but before starting components.
     */
    public SimpleThrottleRegistry throttleRegistry() {
        return throttles;
    }
}
