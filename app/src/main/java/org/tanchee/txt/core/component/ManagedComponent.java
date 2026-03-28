package org.tanchee.txt.core.component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.health.ComponentHealth;
import org.tanchee.txt.core.health.HealthStatus;
import org.tanchee.txt.core.state.ComponentState;
import org.tanchee.txt.core.stats.ComponentStats;

public abstract class ManagedComponent implements Component {
    private final ComponentId id;
    private final ComponentMetadata metadata;

    protected ComponentContext context;
    protected ComponentStats stats;

    protected ManagedComponent(
        String id,
        ComponentMetadata metadata
    ) {
        this.id = new ComponentId(id);
        this.metadata = metadata;
    }

    @Override
    public ComponentId id() {
        return id;
    }

    @Override
    public ComponentMetadata metadata() {
        return metadata;
    }

    @Override
    public CompletionStage<Void> start(ComponentContext context) {
        this.context = context;
        this.stats = context.stats().get(id);

        context.state().setState(id, ComponentState.STARTING);

        try {
            doStart();

            context.state().setState(id, ComponentState.RUNNING);

            context.state().setHealth(
                id, 
                new ComponentHealth(
                    HealthStatus.HEALTHY,
                    "Running",
                    Instant.now(),
                    Map.of()
            ));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            context.state().setState(id, ComponentState.FAILED);

            context.state().setHealth(
                id,
                new ComponentHealth(
                    HealthStatus.UNHEALTHY,
                    e.getMessage(),
                    Instant.now(),
                    Map.of()
            ));

            return CompletableFuture.failedFuture(e);
        }
    }

    protected abstract void doStart() throws Exception;

    @Override
    public CompletionStage<Void> stop() {
        context.state().setState(id, ComponentState.STOPPING);
        return CompletableFuture.runAsync(() -> {
            doStop();
            context.state().setState(id, ComponentState.STOPPED);
        });
    }

    protected abstract void doStop();

    @Override
    public ComponentState state() {
        return context.state().getState(id);
    }

    @Override
    public ComponentHealth health() {
        return context.state().getHealth(id);
    }
}
