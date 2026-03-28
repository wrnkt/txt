package org.tanchee.txt.core.processor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.component.Component;
import org.tanchee.txt.core.component.ComponentContext;
import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.event.Event;
import org.tanchee.txt.core.runtime.RuntimeControl;
import org.tanchee.txt.core.throttle.ThrottleHandle;

public abstract class ManagedProcessor<E extends Event> implements Component {
    private final ComponentId id;
    private final Class<E> eventType;
    protected ComponentContext context;

    protected ManagedProcessor(
        String id,
        Class<E> eventType
    ) {
        this.id = new ComponentId(id);
        this.eventType = eventType;
    }

    @Override
    public ComponentId id() {
        return id;
    }

    @Override
    public CompletionStage<Void> start(ComponentContext context) {
        this.context = context;

        context.eventBus().subscribe(eventType, event -> {
            RuntimeControl control = context.config().get(id, RuntimeControl.class);

            if (!control.enabled()) {
                return CompletableFuture.completedFuture(null);
            }

            ThrottleHandle throttle = context.throttles().get(id);

            if (!throttle.tryAcquire()) {
                return CompletableFuture.completedFuture(null);
            }

            return process(event, control);
        });

        return CompletableFuture.completedFuture(null);
    }

    protected abstract CompletionStage<Void> process(
        E event, 
        RuntimeControl control
    );
}
