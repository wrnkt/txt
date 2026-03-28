package org.tanchee.txt.core.monitoring;

import org.tanchee.txt.core.component.ComponentId;

public interface ComponentStatsRegistry {
    ComponentStats get(ComponentId id);
}
