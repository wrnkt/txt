package org.tanchee.txt.core.throttle;

import org.tanchee.txt.core.component.ComponentId;

public interface ThrottleRegistry {
    ThrottleHandle get(ComponentId id);
}
