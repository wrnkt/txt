package org.tanchee.txt.core.processor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.component.Component;
import org.tanchee.txt.core.component.ComponentContext;
import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.component.ComponentMetadata;
import org.tanchee.txt.core.component.ManagedComponent;
import org.tanchee.txt.core.event.Event;
import org.tanchee.txt.core.monitoring.ComponentHealth;
import org.tanchee.txt.core.monitoring.ComponentStats;
import org.tanchee.txt.core.monitoring.HealthStatus;
import org.tanchee.txt.core.runtime.RuntimeControl;
import org.tanchee.txt.core.state.ComponentState;
import org.tanchee.txt.core.throttle.ThrottleHandle;

public abstract class ManagedProcessor<E extends Event> extends ManagedComponent {

    private final Class<E> eventType;

    protected ManagedProcessor(
        String id,
        ComponentMetadata metadata,
        Class<E> eventType
    ) {
        super(id, metadata);
        this.eventType = eventType;
    }

    @Override
    public CompletionStage<Void> start(ComponentContext context) {
        this.context = context;

        context.eventBus().subscribe(eventType, event -> {
            RuntimeControl control = 
                    context.config().get(id(), RuntimeControl.class);

            if (!control.enabled()) {
                context.state().setState(id(), ComponentState.DISABLED);
                return CompletableFuture.completedFuture(null);
            }

            if (!context.throttles().get(id()).tryAcquire()) {
                return CompletableFuture.completedFuture(null);
            }

            stats.workerStarted();

            return process(event)
                .whenComplete((ignored, error) -> {
                    stats.workerFinished();

                if (error == null) {
                    stats.processed();
                } else {
                    stats.failed();
                    context.state().setHealth(
                        id(),
                        new ComponentHealth(
                            HealthStatus.DEGRADED,
                            error.getMessage(),
                            Instant.now(),
                            Map.of()
                        )
                    );
                }
            });
        });

        return CompletableFuture.completedFuture(null);
    }

    protected abstract CompletionStage<Void> process(E event);
}
