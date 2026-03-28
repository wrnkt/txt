package org.tanchee.txt.core.component;

import java.util.concurrent.Executor;

import org.tanchee.txt.core.config.ConfigHandle;
import org.tanchee.txt.core.event.EventBus;
import org.tanchee.txt.core.state.ComponentStateStore;
import org.tanchee.txt.core.monitoring.ComponentStatsRegistry;
import org.tanchee.txt.core.throttle.ThrottleRegistry;

public interface ComponentContext {
    EventBus eventBus();
    ConfigHandle config();
    ThrottleRegistry throttles();
    ComponentStateStore state();
    ComponentStatsRegistry stats();
    Executor executor();
}
