package org.tanchee.txt.core.component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.monitoring.ComponentHealth;
import org.tanchee.txt.core.monitoring.HealthStatus;
import org.tanchee.txt.core.state.ComponentState;
import org.tanchee.txt.core.monitoring.ComponentStats;

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
        this.stats = context.stats().get(id());

        setState(ComponentState.STARTING);

        try {
            doStart();

            setState(ComponentState.RUNNING);

            setHealth(
                new ComponentHealth(
                    HealthStatus.HEALTHY,
                    "Running",
                    Instant.now(),
                    Map.of()
            ));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            setState(ComponentState.FAILED);

            setHealth(
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
        setState(ComponentState.STOPPING);
        return CompletableFuture.runAsync(() -> {
            doStop();
            setState(ComponentState.STOPPED);
        });
    }

    protected abstract void doStop();

    private void setState(ComponentState state) {
        context.state().setState(id(), state);
    }

    private void setHealth(ComponentHealth health) {
        context.state().setHealth(id(), health);
    }

    @Override
    public ComponentState state() {
        return context.state().getState(id());
    }

    @Override
    public ComponentHealth health() {
        return context.state().getHealth(id());
    }
}
