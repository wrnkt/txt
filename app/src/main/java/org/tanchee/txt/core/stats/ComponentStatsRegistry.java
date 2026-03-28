package org.tanchee.txt.core.stats;

import org.tanchee.txt.core.component.ComponentId;

public interface ComponentStatsRegistry {
    ComponentStats get(ComponentId id);
}
