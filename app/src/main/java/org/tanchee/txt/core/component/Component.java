package org.tanchee.txt.core.component;

import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.monitoring.ComponentHealth;
import org.tanchee.txt.core.state.ComponentState;

public interface Component {
    ComponentId id();
    ComponentMetadata metadata();

    CompletionStage<Void> start(ComponentContext context);
    CompletionStage<Void> stop();

    ComponentState state();
    ComponentHealth health();
}
